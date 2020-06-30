package com.webank.charity;

import com.alibaba.druid.sql.visitor.functions.Char;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.charity.contract.Item;
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
import java.util.*;

@SpringBootApplication
@RestController
public class CharityApplication {
    CharityApplication() throws Exception {
        initialize();
    }

    // 此处是照搬asset app的部分
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
            TransactionReceipt trans= charity.registerUser(name, phone).send();
            List<Charity.RegisterUserEventEventResponse> responses = charity.getRegisterUserEventEvents(trans);
            return privateKey;
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

    //返回用户name
    // privateKey: 私钥
    // 返回: 用户信息name
    @GetMapping("/getUserName")
    public String getUserName(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            String name=charity.getName().send();
            return name;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    //返回用户phone
    // privateKey: 私钥
    // 返回: 用户phone
    @GetMapping("/getUserPhone")
    public String getUserPhone(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            String phone=charity.getPhone().send();
            return phone;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    //返回用户balance
    // privateKey: 私钥
    // 返回: 用户balance
    @GetMapping("/getUserBalance")
    public BigInteger getUserInfo(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            BigInteger balance=charity.getBalance().send();
            return balance;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    //返回用户ownItemsId
    // privateKey: 私钥
    // 返回: 用户ownItemsId
    @GetMapping("/getUserOwnItemsId")
    public String getUserOwnItemsId(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        // 初始化Web3j对象
        Web3j web3j = Web3j.build(channelEthereumService, 1);

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            List<BigInteger> ownItemsId=charity.getOwnItemsId().send();
            return ownItemsId.toString();
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    //返回用户PartItemsId
    // privateKey: 私钥
    // 返回: 用户PartItemsId
    @GetMapping("/getUserPartItemsId")
    public String getUserPartItemsId(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        // 初始化Web3j对象
        Web3j web3j = Web3j.build(channelEthereumService, 1);

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            List<BigInteger> partItemsId=charity.getpartItemsId().send();
            return partItemsId.toString();
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }



    // 发起项目
    //    item_id: 项目ID int
    //    publisher_name : 发起人 string
    //    item_name  : 项目名称 string
    //    beneficiary_name : 受益人 string
    //    target_amount : 目标金额 int
    //    description : 描述 string
    // 返回:
    //             0 项目注册成功
    //            -1 项目id已存在
    //            -2 其他错误
    @RequestMapping("/publish")
    public String publish(@RequestParam(value = "privateKey", required=true) String privateKey,
                          @RequestParam(value = "item_name", required = true) String item_name,
                          @RequestParam(value = "beneficiary_name", required = true) String beneficiary_name,
                          @RequestParam(value = "target_amount", required = true) BigInteger target,
                          @RequestParam(value = "description", defaultValue = "No description.") String description)
    {
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();
        //result
        String res="null";
        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String address0 = charity.getMsgSender().send();
            TransactionReceipt trans= charity.registerItem(item_name, address0, target,description).send();
            List<Charity.RegisterItemEventEventResponse> responses = charity.getRegisterItemEventEvents(trans);
            BigInteger ret_code =responses.get(0).ret_code;
            BigInteger item_id = responses.get(0).id;

            res =ret_code.toString() + "," +item_id.toString() + "," +address0;
            return res;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
        }
        return res;
    }

    // 根据item_id得到publisher_address
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到publisher_address
    @GetMapping("/getItemAddress")
    public String getItemAddress(@RequestParam(value = "privateKey", required=true) String privateKey,
                               @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String address0 = charity.getItem0(item_id).send().getValue4();

            return address0;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 根据item_id得到publisher_name
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到publisher_name
    @GetMapping("/get_item_publisher_name")
    public String get_item_publisher_name(@RequestParam(value = "privateKey", required=true) String privateKey,
                                 @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String publisher_name = charity.getItem0(item_id).send().getValue2();

            return publisher_name;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 根据item_id得到item_name
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到item_name
    @GetMapping("/get_item_item_name")
    public String get_item_item_name(@RequestParam(value = "privateKey", required=true) String privateKey,
                                          @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String item_name = charity.getItem0(item_id).send().getValue3();
            return item_name;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 根据item_id得到target_amount
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到target_amount
    @GetMapping("/get_item_target_amount")
    public BigInteger get_item_target_amount(@RequestParam(value = "privateKey", required=true) String privateKey,
                                     @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            BigInteger target_amount = charity.getItem0(item_id).send().getValue5();
            return target_amount;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 根据item_id得到description
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到description
    @GetMapping("/get_item_description")
    public String get_item_description(@RequestParam(value = "privateKey", required=true) String privateKey,
                                             @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String description = charity.getItem1(item_id).send().getValue2();
            return description;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 根据item_id得到donation_amount
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到donation_amount
    @GetMapping("/get_item_donation_amount")
    public BigInteger get_item_donation_amount(@RequestParam(value = "privateKey", required=true) String privateKey,
                                             @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            BigInteger donation_amount = charity.getItem1(item_id).send().getValue3();
            return donation_amount;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }
    // 根据item_id得到num_of_donation
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到num_of_donation
    @GetMapping("/get_item_num_of_donation")
    public BigInteger get_item_num_of_donation(@RequestParam(value = "privateKey", required=true) String privateKey,
                                               @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            BigInteger num_of_donation = charity.getItem1(item_id).send().getValue4();
            return num_of_donation;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }
    // 根据item_id得到status
// privateKey: 私钥
// item_id
// 返回: 根据item_id得到status
    @GetMapping("/get_item_status")
    public String get_item_status(@RequestParam(value = "privateKey", required=true) String privateKey,
                                       @RequestParam(value = "item_id", required=true) BigInteger item_id
    ) throws Exception {

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            String status = charity.getItem1(item_id).send().getValue5();
            return status;
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }
    // 得到所有项目
// privateKey: 私钥
// 返回: allItemsId
    @GetMapping("/getAllItemsId")
    public String getAllItemsId(@RequestParam(value = "privateKey", required=true) String privateKey
    ) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        // 初始化Web3j对象
        Web3j web3j = Web3j.build(channelEthereumService, 1);

        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);
            List<BigInteger> allItemsId=charity.getAllItemsId().send();
            return allItemsId.toString();
        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

    // 下架项目
    // id: 项目ID
    // 返回: 错误信息
    @RequestMapping("/cancelItem")
    public String cancelItem(@RequestParam(value = "privateKey", required=true) String privateKey,
                             @RequestParam(value = "item_id", required = true) BigInteger item_id)
            throws Exception{
        //通过指定外部账户私钥使用指定的外部账户
        Credentials credentials = GenCredential.create(privateKey);
        //账户地址
        String address = credentials.getAddress();

        try {
            String contractAddress = loadOrDeploy();
            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            Item item = Item.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            System.out.println(" load Charity success, contract address is " + contractAddress);
            recordAssetAddr(contractAddress);

            TransactionReceipt trans= charity.cancelItem(item_id).send();
            List<Charity.CancelItemEventEventResponse> responses = charity.getCancelItemEventEvents(trans);
            BigInteger ret_code =responses.get(0).ret_code;
            return ret_code.toString();

        } catch (Exception e2) {
            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
            return null;
        }
    }

//    // 重新上架项目
//    // id: 项目ID
//    // 返回: 错误信息
//    @RequestMapping("/pushItem")
//    public String pubshItem(@RequestParam(value = "privateKey", required=true) String privateKey,
//                             @RequestParam(value = "item_id", required = true) BigInteger item_id)
//            throws Exception{
//        //通过指定外部账户私钥使用指定的外部账户
//        Credentials credentials = GenCredential.create(privateKey);
//        //账户地址
//        String address = credentials.getAddress();
//
//        try {
//            String contractAddress = loadOrDeploy();
//            Charity charity = Charity.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
//            Item item = Item.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
//            System.out.println(" load Charity success, contract address is " + contractAddress);
//            recordAssetAddr(contractAddress);
//
//            TransactionReceipt trans= charity.pushItem(item_id).send();
//            List<Charity.CancelItemEventEventResponse> responses = charity.getPushItemEventEvents(trans);
//            BigInteger ret_code =responses.get(0).ret_code;
//            return ret_code.toString();
//
//        } catch (Exception e2) {
//            System.out.println(" load Charity contract failed, error message is  " + e2.getMessage());
//            return null;
//        }
//    }


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
            TransactionReceipt trans = charity.donate(amount, id).send();
            List<Charity.DonateEventEventResponse> responses = charity.getDonateEventEvents(trans);
            BigInteger ret_code = responses.get(0).ret_code;
            BigInteger rid = responses.get(0).id;
            logger.info("ret: {}, id: {}", ret_code, id);
            if(ret_code.equals(BigInteger.ZERO))
                return String.format("{succeed:%d, id:%d}", 1, rid);
            else if(ret_code.equals(new BigInteger("-1")))
                return String.format("{succeed:%d, error:\"%s\"}", 0, "Nonexistent item");
            else if(ret_code.equals(new BigInteger("-2")))
                return String.format("{succeed:%d, error:\"%s\"}", 0, "Balance not enough");
            else if(ret_code.equals(new BigInteger("-3")))
                return String.format("{succeed:%d, error:\"%s\"}", 0, "Item closed");
            else return String.format("{succeed:%d, error:\"%s\"}", 0, "Unhandled error");
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
    @RequestMapping("/result")
    public String infoJudge(@RequestParam(value = "id") BigInteger id) {
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