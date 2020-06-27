package org.fisco.bcos.charity.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Bool;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple1;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tx.Contract;
import org.fisco.bcos.web3j.tx.TransactionManager;
import org.fisco.bcos.web3j.tx.gas.ContractGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version none.
 */
@SuppressWarnings("unchecked")
public class Charity extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50600060038190555061251f806100286000396000f300608060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630cdd53f6146100885780633d6c1bb9146100d35780634baef160146101e657806351563f8e1461021357806373004ca01461024057806393e54a561461036b578063d38d8b7d146104a6575b600080fd5b34801561009457600080fd5b506100bd60048036038101908080359060200190929190803590602001909291905050506105eb565b6040518082815260200191505060405180910390f35b3480156100df57600080fd5b506101d0600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929080359060200190929190505050610e33565b6040518082815260200191505060405180910390f35b3480156101f257600080fd5b5061021160048036038101908080359060200190929190505050611320565b005b34801561021f57600080fd5b5061023e60048036038101908080359060200190929190505050611911565b005b34801561024c57600080fd5b5061026b60048036038101908080359060200190929190505050611c67565b60405180806020018681526020018060200185815260200184151515158152602001838103835288818151815260200191508051906020019080838360005b838110156102c55780820151818401526020810190506102aa565b50505050905090810190601f1680156102f25780820380516001836020036101000a031916815260200191505b50838103825286818151815260200191508051906020019080838360005b8381101561032b578082015181840152602081019050610310565b50505050905090810190601f1680156103585780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b34801561037757600080fd5b506104a4600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050611e6b565b005b3480156104b257600080fd5b506104d16004803603810190808035906020019092919050505061209c565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018060200180602001838103835285818151815260200191508051906020019080838360005b8381101561054757808201518184015260208101905061052c565b50505050905090810190601f1680156105745780820380516001836020036101000a031916815260200191505b50838103825284818151815260200191508051906020019080838360005b838110156105ad578082015181840152602081019050610592565b50505050905090810190601f1680156105da5780820380516001836020036101000a031916815260200191505b509550505050505060405180910390f35b60006105f5612267565b600080600060018681548110151561060957fe5b906000526020600020906008020161010060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107135780601f106106e857610100808354040283529160200191610713565b820191906000526020600020905b8154815290600101906020018083116106f657829003601f168201915b50505050508152602001600282018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107b55780601f1061078a576101008083540402835291602001916107b5565b820191906000526020600020905b81548152906001019060200180831161079857829003601f168201915b50505050508152602001600382018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108575780601f1061082c57610100808354040283529160200191610857565b820191906000526020600020905b81548152906001019060200180831161083a57829003601f168201915b5050505050815260200160048201548152602001600582018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156109035780601f106108d857610100808354040283529160200191610903565b820191906000526020600020905b8154815290600101906020018083116108e657829003601f168201915b50505050508152602001600682015481526020016007820160009054906101000a900460ff16151515158152505093508360000151925033915060008711801561098d57506000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206003015487105b15610e2857866000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060030160008282540392505081905550866000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060030160008282540192505081905550868460800181815101915081815250504233600354604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166c010000000000000000000000000281526014018281526020019350505050604051809103902060019004905060036000815480929190600101919050555060a0604051908101604052808373ffffffffffffffffffffffffffffffffffffffff1681526020018473ffffffffffffffffffffffffffffffffffffffff168152602001828152602001888152602001858152506002600083815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550604082015181600201556060820151816003015560808201518160040160008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001019080519060200190610c309291906122c5565b506040820151816002019080519060200190610c4d9291906122c5565b506060820151816003019080519060200190610c6a9291906122c5565b506080820151816004015560a0820151816005019080519060200190610c919291906122c5565b5060c0820151816006015560e08201518160070160006101000a81548160ff02191690831515021790555050509050506000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600801849080600181540180825580915050906001820390600052602060002090600802016000909192909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001019080519060200190610d919291906122c5565b506040820151816002019080519060200190610dae9291906122c5565b506060820151816003019080519060200190610dcb9291906122c5565b506080820151816004015560a0820151816005019080519060200190610df29291906122c5565b5060c0820151816006015560e08201518160070160006101000a81548160ff021916908315150217905550505050809450610e29565b5b5050505092915050565b60006001610100604051908101604052803373ffffffffffffffffffffffffffffffffffffffff1681526020018781526020018681526020016000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610f415780601f10610f1657610100808354040283529160200191610f41565b820191906000526020600020905b815481529060010190602001808311610f2457829003601f168201915b5050505050815260200160008152602001858152602001848152602001600115158152509080600181540180825580915050906001820390600052602060002090600802016000909192909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550","6020820151816001019080519060200190610ff39291906122c5565b5060408201518160020190805190602001906110109291906122c5565b50606082015181600301908051906020019061102d9291906122c5565b506080820151816004015560a08201518160050190805190602001906110549291906122c5565b5060c0820151816006015560e08201518160070160006101000a81548160ff0219169083151502179055505050506000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600701610100604051908101604052803373ffffffffffffffffffffffffffffffffffffffff1681526020018781526020018681526020016000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156111cd5780601f106111a2576101008083540402835291602001916111cd565b820191906000526020600020905b8154815290600101906020018083116111b057829003601f168201915b5050505050815260200160008152602001858152602001848152602001600115158152509080600181540180825580915050906001820390600052602060002090600802016000909192909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550602082015181600101908051906020019061127f9291906122c5565b50604082015181600201908051906020019061129c9291906122c5565b5060608201518160030190805190602001906112b99291906122c5565b506080820151816004015560a08201518160050190805190602001906112e09291906122c5565b5060c0820151816006015560e08201518160070160006101000a81548160ff02191690831515021790555050505060018080549050039050949350505050565b611328612345565b6000806000611335612267565b6002600087815260200190815260200160002060a060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160028201548152602001600382015481526020016004820161010060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156115155780601f106114ea57610100808354040283529160200191611515565b820191906000526020600020905b8154815290600101906020018083116114f857829003601f168201915b50505050508152602001600282018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156115b75780601f1061158c576101008083540402835291602001916115b7565b820191906000526020600020905b81548152906001019060200180831161159a57829003601f168201915b50505050508152602001600382018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156116595780601f1061162e57610100808354040283529160200191611659565b820191906000526020600020905b81548152906001019060200180831161163c57829003601f168201915b5050505050815260200160048201548152602001600582018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156117055780601f106116da57610100808354040283529160200191611705565b820191906000526020600020905b8154815290600101906020018083116116e857829003601f168201915b50505050508152602001600682015481526020016007820160009054906101000a900460ff16151515158152505081525050945084600001519350846020015192508460600151915084608001519050816000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060030160008282540192505081905550816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600301600082825403925050819055508181608001818151039150818152505060026000878152602001908152602001600020600080820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160009055600382016000905560048201600080820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556001820160006118b191906123a8565b6002820160006118c191906123a8565b6003820160006118d191906123a8565b60048201600090556005820160006118e991906123a8565b60068201600090556007820160006101000a81549060ff021916905550505050505050505050565b611919612267565b60018281548110151561192857fe5b906000526020600020906008020161010060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611a325780601f10611a0757610100808354040283529160200191611a32565b820191906000526020600020905b815481529060010190602001808311611a1557829003601f168201915b50505050508152602001600282018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611ad45780601f10611aa957610100808354040283529160200191611ad4565b820191906000526020600020905b815481529060010190602001808311611ab757829003601f168201915b50505050508152602001600382018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611b765780601f10611b4b57610100808354040283529160200191611b76565b820191906000526020600020905b815481529060010190602001808311611b5957829003601f168201915b5050505050815260200160048201548152602001600582018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611c225780601f10611bf757610100808354040283529160200191611c22565b820191906000526020600020905b815481529060010190602001808311611c0557829003601f168201915b50505050508152602001600682015481526020016007820160009054906101000a900460ff161515151581525050905060008160e00190151590811515815250505050565b606060006060600080600186815481101515611c7f57fe5b9060005260206000209060080201600301600187815481101515611c9f57fe5b906000526020600020906008020160040154600188815481101515611cc057fe5b9060005260206000209060080201600501600189815481101515611ce057fe5b90600052602060002090600802016006015460018a815481101515611d0157fe5b906000526020600020906008020160070160009054906101000a900460ff16848054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611db55780601f10611d8a57610100808354040283529160200191611db5565b820191906000526020600020905b815481529060010190602001808311611d9857829003601f168201915b50505050509450828054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015611e515780601f10611e2657610100808354040283529160200191611e51565b820191906000526020600020905b815481529060010190602001808311611e3457829003601f168201915b505050505092509450945094509450945091939590929450565b836000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002019080519060200190611ec09291906123f0565b50826000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206004019080519060200190611f169291906123f0565b506040805190810160405280600481526020017f55736572000000000000000000000000000000000000000000000000000000008152506000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001019080519060200190611fa19291906123f0565b506103e86000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152","60200190815260200160002060030181905550816000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600501908051906020019061203f9291906123f0565b50806000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060060190805190602001906120959291906123f0565b5050505050565b60006060806001848154811015156120b057fe5b906000526020600020906008020160000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff166001858154811015156120f157fe5b906000526020600020906008020160010160018681548110151561211157fe5b9060005260206000209060080201600201818054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156121b75780601f1061218c576101008083540402835291602001916121b7565b820191906000526020600020905b81548152906001019060200180831161219a57829003601f168201915b50505050509150808054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156122535780601f1061222857610100808354040283529160200191612253565b820191906000526020600020905b81548152906001019060200180831161223657829003601f168201915b505050505090509250925092509193909250565b61010060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff1681526020016060815260200160608152602001606081526020016000815260200160608152602001600081526020016000151581525090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061230657805160ff1916838001178555612334565b82800160010185558215612334579182015b82811115612333578251825591602001919060010190612318565b5b5090506123419190612470565b5090565b61018060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff16815260200160008152602001600081526020016123a2612495565b81525090565b50805460018160011615610100020316600290046000825580601f106123ce57506123ed565b601f0160209004906000526020600020908101906123ec9190612470565b5b50565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061243157805160ff191683800117855561245f565b8280016001018555821561245f579182015b8281111561245e578251825591602001919060010190612443565b5b50905061246c9190612470565b5090565b61249291905b8082111561248e576000816000905550600101612476565b5090565b90565b61010060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff16815260200160608152602001606081526020016060815260200160008152602001606081526020016000815260200160001515815250905600a165627a7a7230582093e3b6917df4b98b3e9af9cc948b38b0a09bf7c2a00ca1b6f50efc57d90e50c20029"};

    public static final String BINARY = String.join("", BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":false,\"inputs\":[{\"name\":\"_money\",\"type\":\"uint256\"},{\"name\":\"_numb\",\"type\":\"uint256\"}],\"name\":\"donate\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_ID\",\"type\":\"string\"},{\"name\":\"_iname\",\"type\":\"string\"},{\"name\":\"_describe\",\"type\":\"string\"},{\"name\":\"_target\",\"type\":\"uint256\"}],\"name\":\"registerItem\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_rid\",\"type\":\"uint256\"}],\"name\":\"undoDonate\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_numb\",\"type\":\"uint256\"}],\"name\":\"cancelItem\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"numb\",\"type\":\"uint256\"}],\"name\":\"getItemDetails\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_name\",\"type\":\"string\"},{\"name\":\"_phone\",\"type\":\"string\"},{\"name\":\"_location\",\"type\":\"string\"},{\"name\":\"_email\",\"type\":\"string\"}],\"name\":\"registerUser\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"numb\",\"type\":\"uint256\"}],\"name\":\"getItemBase\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]"};

    public static final String ABI = String.join("", ABI_ARRAY);

    public static final TransactionDecoder transactionDecoder = new TransactionDecoder(ABI, BINARY);

    public static final String FUNC_DONATE = "donate";

    public static final String FUNC_REGISTERITEM = "registerItem";

    public static final String FUNC_UNDODONATE = "undoDonate";

    public static final String FUNC_CANCELITEM = "cancelItem";

    public static final String FUNC_GETITEMDETAILS = "getItemDetails";

    public static final String FUNC_REGISTERUSER = "registerUser";

    public static final String FUNC_GETITEMBASE = "getItemBase";

    @Deprecated
    protected Charity(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Charity(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Charity(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Charity(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static TransactionDecoder getTransactionDecoder() {
        return transactionDecoder;
    }

    public RemoteCall<TransactionReceipt> donate(BigInteger _money, BigInteger _numb) {
        final Function function = new Function(
                FUNC_DONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_money),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_numb)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void donate(BigInteger _money, BigInteger _numb, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_DONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_money),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_numb)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String donateSeq(BigInteger _money, BigInteger _numb) {
        final Function function = new Function(
                FUNC_DONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_money),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_numb)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple2<BigInteger, BigInteger> getDonateInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_DONATE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue()
        );
    }

    public Tuple1<BigInteger> getDonateOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_DONATE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public RemoteCall<TransactionReceipt> registerItem(String _ID, String _iname, String _describe, BigInteger _target) {
        final Function function = new Function(
                FUNC_REGISTERITEM,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_ID),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_iname),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_describe),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_target)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void registerItem(String _ID, String _iname, String _describe, BigInteger _target, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_REGISTERITEM,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_ID),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_iname),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_describe),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_target)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String registerItemSeq(String _ID, String _iname, String _describe, BigInteger _target) {
        final Function function = new Function(
                FUNC_REGISTERITEM,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_ID),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_iname),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_describe),
                        new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_target)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple4<String, String, String, BigInteger> getRegisterItemInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_REGISTERITEM,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple4<String, String, String, BigInteger>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (BigInteger) results.get(3).getValue()
        );
    }

    public Tuple1<BigInteger> getRegisterItemOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_REGISTERITEM,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public RemoteCall<TransactionReceipt> undoDonate(BigInteger _rid) {
        final Function function = new Function(
                FUNC_UNDODONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_rid)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void undoDonate(BigInteger _rid, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_UNDODONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_rid)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String undoDonateSeq(BigInteger _rid) {
        final Function function = new Function(
                FUNC_UNDODONATE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(_rid)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple1<BigInteger> getUndoDonateInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_UNDODONATE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public void cancelItem(BigInteger _numb) {
        throw new RuntimeException("cannot call constant function with void return type");
    }

    public RemoteCall<Tuple5<String, BigInteger, String, BigInteger, Boolean>> getItemDetails(BigInteger numb) {
        final Function function = new Function(FUNC_GETITEMDETAILS,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(numb)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        return new RemoteCall<Tuple5<String, BigInteger, String, BigInteger, Boolean>>(
                new Callable<Tuple5<String, BigInteger, String, BigInteger, Boolean>>() {
                    @Override
                    public Tuple5<String, BigInteger, String, BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, BigInteger, String, BigInteger, Boolean>(
                                (String) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                (String) results.get(2).getValue(),
                                (BigInteger) results.get(3).getValue(),
                                (Boolean) results.get(4).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> registerUser(String _name, String _phone, String _location, String _email) {
        final Function function = new Function(
                FUNC_REGISTERUSER,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_name),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_phone),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_location),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_email)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public void registerUser(String _name, String _phone, String _location, String _email, TransactionSucCallback callback) {
        final Function function = new Function(
                FUNC_REGISTERUSER,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_name),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_phone),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_location),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_email)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String registerUserSeq(String _name, String _phone, String _location, String _email) {
        final Function function = new Function(
                FUNC_REGISTERUSER,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_name),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_phone),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_location),
                        new org.fisco.bcos.web3j.abi.datatypes.Utf8String(_email)),
                Collections.<TypeReference<?>>emptyList());
        return createTransactionSeq(function);
    }

    public Tuple4<String, String, String, String> getRegisterUserInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_REGISTERUSER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());;
        return new Tuple4<String, String, String, String>(

                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (String) results.get(3).getValue()
        );
    }

    public RemoteCall<Tuple3<String, String, String>> getItemBase(BigInteger numb) {
        final Function function = new Function(FUNC_GETITEMBASE,
                Arrays.<Type>asList(new org.fisco.bcos.web3j.abi.datatypes.generated.Uint256(numb)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteCall<Tuple3<String, String, String>>(
                new Callable<Tuple3<String, String, String>>() {
                    @Override
                    public Tuple3<String, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, String>(
                                (String) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (String) results.get(2).getValue());
                    }
                });
    }

    @Deprecated
    public static Charity load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Charity(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Charity load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Charity(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Charity load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Charity(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Charity load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Charity(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Charity> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Charity.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Charity> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Charity.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Charity> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Charity.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Charity> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Charity.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}