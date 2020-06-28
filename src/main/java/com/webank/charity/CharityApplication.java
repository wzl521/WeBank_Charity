package com.webank.charity;

import com.alibaba.druid.sql.visitor.functions.Char;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.charity.contract.Charity;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;

@SpringBootApplication
@RestController
public class CharityApplication {
    CharityApplication() throws Exception {
        initialize();
    }

    // 此处是照办asset app的部分
    private Web3j web3j;

    private Credentials s_credentials;

    static Logger logger = LoggerFactory.getLogger(CharityApplication.class);

    private static BigInteger gasPrice = new BigInteger("30000000");
    private static BigInteger gasLimit = new BigInteger("30000000");

    public static void main(String[] args) throws Exception {
        SpringApplication.run(CharityApplication.class, args);
    }

    public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty("address", address);
        final Resource contractResource = new ClassPathResource("contract.properties");
        FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
        prop.store(fileOutputStream, "contract address");
    }

    public String loadAssetAddr() throws Exception {
        // load Asset contact address from contract.properties
        Properties prop = new Properties();
        final Resource contractResource = new ClassPathResource("contract.properties");
        prop.load(contractResource.getInputStream());

        String contractAddress = prop.getProperty("address");
        if (contractAddress == null || contractAddress.trim().equals("")) {
            throw new Exception(" load Asset contract address failed, please deploy it first. ");
        }
        logger.info(" load Asset address from contract.properties, address is {}", contractAddress);
        return contractAddress;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public Credentials getCredentials() {
        return s_credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.s_credentials = credentials;
    }

    public void initialize() throws Exception {

        // init the Service
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService, 1);

        // init Credentials
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());

        setCredentials(credentials);
        setWeb3j(web3j);

        logger.debug(" web3j is " + web3j + " ,credentials is " + credentials);
    }

    public void deployAssetAndRecordAddr() {
        try {
            Charity asset = Charity.deploy(web3j, s_credentials, new StaticGasProvider(gasPrice, gasLimit)).send();
            System.out.println(" deploy Asset success, contract address is " + asset.getContractAddress());

            recordAssetAddr(asset.getContractAddress());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println(" deploy Asset contract failed, error message is  " + e.getMessage());
        }
    }

    // 检查是否部署合约并返回合约地址
    public String loadOrDeploy() {
        // 检查是否部署合约
        String address = "";
        try {
            address = loadAssetAddr();
        } catch (Exception e1) {
            try {
                Charity asset = Charity.deploy(web3j, s_credentials, new StaticGasProvider(gasPrice, gasLimit)).send();
                address = asset.getContractAddress();
                System.out.println(" deploy Charity success, contract address is " + address);
                recordAssetAddr(address);
            } catch (Exception e2) {
                System.out.println(" deploy Charity contract failed, error message is  " + e2.getMessage());
            }
        } finally {
            return address;
        }
    }

    // 测试用页面
    @GetMapping("/test")
    public String test(@RequestParam(value = "privateKey", required = true) String privateKey) throws Exception {
        try {
            String contractAddress = loadOrDeploy();
            Credentials credentials = GenCredential.create(privateKey);
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            return new String("Successful");
        } catch (Exception e) {
            return new String("Failed");
        }
    }

    // 开户接口
    // name: 自然人名字
    // phone: 电话
    // 返回: 私钥
    @GetMapping("/gen_account")
    public String genAccount(@RequestParam(value = "name", required = true) String name,
                             @RequestParam(value = "phone", required = true) String phone
) {
        //创建普通账户
        EncryptType.encryptType = 0;
        Credentials credentials = GenCredential.create();
        //账户地址
        String address = credentials.getAddress();
        //账户私钥
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
        //账户公钥
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            charity.registerUser(name, phone);
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
        }

        //return new String("Successful.");
        return privateKey;
    }

    //登陆接口
    // privateKey: 私钥
    // 返回: 登陆信息
    @GetMapping("/login")
    public String login(@RequestParam(value = "privateKey", required = true) String privateKey
    ) throws Exception {
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();
        //账户公钥
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
        if (credentials != null) {
            return new String("Successful.");
        } else {
            return new String("Failed.");
        }
    }

    //返回用户信息
    // privateKey: 私钥
    // 返回: 用户信息（name,phone,balance,ownItemsId,ownItemsId）
    @GetMapping("/getUserInfo")
    public String getUserInfo(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        Credentials credentials = GenCredential.create(privateKey);

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            return charity.getUserInfo().send().getOutput();
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }

    }

    // 发起项目
    // id: 账户ID
    // name: 项目名
    // string: 描述
    // target: 目标金额
    // 返回: 项目ID
    @RequestMapping("/publish/{id}")
    public BigInteger publish(@PathVariable(value = "id") BigInteger id,
                              @RequestParam(value = "name", required = true) String name,
                              @RequestParam(value = "describe", defaultValue = "No description.") String describe,
                              @RequestParam(value = "target", required = true) BigInteger target) {
        return new BigInteger("0");
    }

    // 下架项目
    // id: 项目ID
    // 返回: 错误信息
    @GetMapping("/withdraw")
    public String withdraw(@RequestParam(value = "id", required = true) BigInteger id) {
        return new String("Successful");
    }

    // 发起捐赠
    // id: 项目ID
    // amount: 捐赠数额
    // 返回：JSON串，包含是否成功/交易ID/失败原因
    @RequestMapping("/donate")
    public String donate(@RequestParam(value = "privateKey", required=true) String privateKey,
            @RequestParam(value = "id", required = true) BigInteger id,
                         @RequestParam(value = "amount", required = true) BigInteger amount) {
        try {
            String contractAddress = loadOrDeploy();
            Credentials credentials = GenCredential.create(privateKey);
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            String rid = charity.donate(amount, id).send().getOutput();

            return String.format("{succeed:%d, id:%s}", 1, rid);
        } catch (Exception e) {
            logger.error("Donate failed! Message: {}.", e.getMessage());
            return String.format("{succeed:%d, error:\"%s\"}", 0, e.getMessage());
        }
    }

    private HashMap<BigInteger, BigInteger> waitForJudge;
    private HashMap<BigInteger, String> judgeOwner;
    private HashMap<BigInteger, Boolean> judgeResult;

    // 反悔，撤销捐赠，发起仲裁。仲裁将发送给管理员（管理员的实现待讨论）
    // privateKey：私钥
    // id: 捐赠交易的ID
    // 返回: 本次仲裁的编号，用于查询仲裁结果。
    @GetMapping("/repent")
    public BigInteger repent(@RequestParam(value = "privateKey", required=true) String privateKey,
                             @RequestParam(value = "id", required = true) BigInteger id) {
        final long time = System.currentTimeMillis();
        BigInteger code = new BigInteger("" + time);
        waitForJudge.put(code, id);
        judgeOwner.put(code, privateKey);
        return code;
    }

    // 仅限管理员使用的功能，仲裁
    // id: 仲裁编号
    // agree: 是否同意撤销捐赠
    @RequestMapping("/judge")
    public void judge(@RequestParam(value = "id", required = true) BigInteger id,
                      @RequestParam(value = "agree", defaultValue = "true") boolean agree) {
        BigInteger tran_id = waitForJudge.get(id);
        String owner = judgeOwner.get(id);
        if (agree) {
            try {
                String address = loadOrDeploy();
                Credentials credentials = GenCredential.create(owner);
                Charity charity = Charity.load(address, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                charity.undoDonate(id).send();
            } catch (Exception e) {
                logger.error("Judge failed! Message: {}.", e.getMessage());
                return;
            }
        }
        judgeResult.put(id, agree);
        waitForJudge.remove(id);
        judgeOwner.remove(id);
    }

    // 查询仲裁结果
    // id: 仲裁编号
    // 返回: 仲裁结果，至少包含: 不存在，尚未仲裁，通过，拒绝。
    @RequestMapping("/result/{id}")
    public String infoJudge(@PathVariable(value = "id") BigInteger id) {
        if (waitForJudge.containsKey(id)) {
            return new String("Judging.");
        } else if (judgeResult.containsKey(id)) {
            if (judgeResult.get(id)) {
                return new String("Succeed.");
            } else {
                return new String("Refuse.");
            }
        } else {
            return new String("Nonexistent.");
        }
    }
}
