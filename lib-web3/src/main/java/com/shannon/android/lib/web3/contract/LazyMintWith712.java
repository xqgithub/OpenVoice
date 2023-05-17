package com.shannon.android.lib.web3.contract;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.2.
 */
@SuppressWarnings("rawtypes")
public class LazyMintWith712 extends Contract {
    public static final String BINARY = "0x6101406040523480156200001257600080fd5b50604051620018503803806200185083398101604081905262000035916200020c565b60408051808201825260058152640312e302e360dc1b60209182015283518482012060e08190527f06c015bd22b4c69690933c1058878ebdfef31f9aaae40bbe86d8a09fe1b2972c6101008190524660a081815285517f8b73c3c69bb8fe3d512ecc4cf759cc79239f7b179b0ffacaa9a75d522b39400f818701819052818801959095526060810193909352608080840192909252308382018190528651808503909201825260c09384019096528051940193909320909252919052610120526200010260003362000129565b600180546001600160a01b0319166001600160a01b039290921691909117905550620002f4565b62000135828262000139565b5050565b6000828152602081815260408083206001600160a01b038516845290915290205460ff1662000135576000828152602081815260408083206001600160a01b03851684529091529020805460ff19166001179055620001953390565b6001600160a01b0316816001600160a01b0316837f2f8788117e7eff1d82e926ec794901d17c78024a50270940304540a733656f0d60405160405180910390a45050565b634e487b7160e01b600052604160045260246000fd5b80516001600160a01b03811681146200020757600080fd5b919050565b600080604083850312156200022057600080fd5b82516001600160401b03808211156200023857600080fd5b818501915085601f8301126200024d57600080fd5b815181811115620002625762000262620001d9565b604051601f8201601f19908116603f011681019083821181831017156200028d576200028d620001d9565b81604052828152602093508884848701011115620002aa57600080fd5b600091505b82821015620002ce5784820184015181830185015290830190620002af565b6000848483010152809650505050620002e9818601620001ef565b925050509250929050565b60805160a05160c05160e051610100516101205161150c620003446000396000610e5401526000610ea301526000610e7e01526000610dd701526000610e0101526000610e2b015261150c6000f3fe6080604052600436106100915760003560e01c80636dc2f261116100595780636dc2f2611461015e57806391d148541461017e578063a217fddf1461019e578063bed79f1c146101b3578063d547741f146101c657600080fd5b806301ffc9a714610096578063248a9ca3146100cb5780632f2ff15d1461010957806336568abe1461012b5780634f3f824f1461014b575b600080fd5b3480156100a257600080fd5b506100b66100b1366004610fde565b6101e6565b60405190151581526020015b60405180910390f35b3480156100d757600080fd5b506100fb6100e6366004611008565b60009081526020819052604090206001015490565b6040519081526020016100c2565b34801561011557600080fd5b50610129610124366004611046565b61021d565b005b34801561013757600080fd5b50610129610146366004611046565b610247565b610129610159366004611172565b6102ca565b34801561016a57600080fd5b5061012961017936600461124b565b61047d565b34801561018a57600080fd5b506100b6610199366004611046565b6104b3565b3480156101aa57600080fd5b506100fb600081565b6101296101c1366004611172565b6104dc565b3480156101d257600080fd5b506101296101e1366004611046565b6105fb565b60006001600160e01b03198216637965db0b60e01b148061021757506301ffc9a760e01b6001600160e01b03198316145b92915050565b60008281526020819052604090206001015461023881610620565b610242838361062d565b505050565b6001600160a01b03811633146102bc5760405162461bcd60e51b815260206004820152602f60248201527f416363657373436f6e74726f6c3a2063616e206f6e6c792072656e6f756e636560448201526e103937b632b9903337b91039b2b63360891b60648201526084015b60405180910390fd5b6102c682826106b1565b5050565b60006102da8b8b348c8c8c610716565b9050866001600160a01b03166103268285858080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152506107ac92505050565b6001600160a01b0316146103705760405162461bcd60e51b8152602060048201526011602482015270496e76616c6964207369676e617475726560781b60448201526064016102b3565b600160009054906101000a90046001600160a01b03166001600160a01b031663f242432a88888e89896040518663ffffffff1660e01b81526004016103b99594939291906112b8565b600060405180830381600087803b1580156103d357600080fd5b505af11580156103e7573d6000803e3d6000fd5b505060015460405163152a902d60e11b8152600481018f9052346024820152600093506001600160a01b039091169150632a55205a906044016040805180830381865afa15801561043c573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061046091906112f2565b91505061046f8834838d6107d0565b505050505050505050505050565b6104886000336104b3565b61049157600080fd5b600280546001600160a01b0319166001600160a01b0392909216919091179055565b6000918252602082815260408084206001600160a01b0393909316845291905290205460ff1690565b60006104ec8b8b348c8c8c610716565b9050886001600160a01b03166105388285858080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152506107ac92505050565b6001600160a01b0316146105825760405162461bcd60e51b8152602060048201526011602482015270496e76616c6964207369676e617475726560781b60448201526064016102b3565b60015460405163d3b9ff7f60e01b81526001600160a01b039091169063d3b9ff7f906105ba9089908f908a908a908f90600401611320565b600060405180830381600087803b1580156105d457600080fd5b505af11580156105e8573d6000803e3d6000fd5b50505050600061046f8a348360006107d0565b60008281526020819052604090206001015461061681610620565b61024283836106b1565b61062a813361097a565b50565b61063782826104b3565b6102c6576000828152602081815260408083206001600160a01b03851684529091529020805460ff1916600117905561066d3390565b6001600160a01b0316816001600160a01b0316837f2f8788117e7eff1d82e926ec794901d17c78024a50270940304540a733656f0d60405160405180910390a45050565b6106bb82826104b3565b156102c6576000828152602081815260408083206001600160a01b0385168085529252808320805460ff1916905551339285917ff6391f5c32d9c69d2a47ea670b442974b53935d1edc7fd64eb21e047a839171b9190a45050565b604080517f6697de68ba13acbc918df3edbe217031a9b63ef8d3466d3e33e0ed35dd96fb8d60208201529081018790526001600160a01b0380871660608301526080820186905280851660a08301526001600160601b03841660c0830152821660e08201526000906107a19061010001604051602081830303815290604052805190602001206109de565b979650505050505050565b60008060006107bb8585610a2c565b915091506107c881610a71565b509392505050565b600060046107df85600161137e565b6107e99190611395565b905060006107f782866113b7565b90506000841561088b578261080c86886113b7565b61081691906113b7565b9150836001600160a01b03168560405160006040518083038185875af1925050503d8060008114610863576040519150601f19603f3d011682016040523d82523d6000602084013e610868565b606091505b5050809150508061088b5760405162461bcd60e51b81526004016102b3906113ca565b6040516001600160a01b038816908390600081818185875af1925050503d80600081146108d4576040519150601f19603f3d011682016040523d82523d6000602084013e6108d9565b606091505b505080915050806108fc5760405162461bcd60e51b81526004016102b3906113ca565b6002546040516001600160a01b03909116908490600081818185875af1925050503d8060008114610949576040519150601f19603f3d011682016040523d82523d6000602084013e61094e565b606091505b505080915050806109715760405162461bcd60e51b81526004016102b3906113ca565b50505050505050565b61098482826104b3565b6102c65761099c816001600160a01b03166014610c27565b6109a7836020610c27565b6040516020016109b89291906113f8565b60408051601f198184030181529082905262461bcd60e51b82526102b39160040161146d565b60006102176109eb610dca565b8360405161190160f01b6020820152602281018390526042810182905260009060620160405160208183030381529060405280519060200120905092915050565b6000808251604103610a625760208301516040840151606085015160001a610a5687828585610ef1565b94509450505050610a6a565b506000905060025b9250929050565b6000816004811115610a8557610a85611480565b03610a8d5750565b6001816004811115610aa157610aa1611480565b03610aee5760405162461bcd60e51b815260206004820152601860248201527f45434453413a20696e76616c6964207369676e6174757265000000000000000060448201526064016102b3565b6002816004811115610b0257610b02611480565b03610b4f5760405162461bcd60e51b815260206004820152601f60248201527f45434453413a20696e76616c6964207369676e6174757265206c656e6774680060448201526064016102b3565b6003816004811115610b6357610b63611480565b03610bbb5760405162461bcd60e51b815260206004820152602260248201527f45434453413a20696e76616c6964207369676e6174757265202773272076616c604482015261756560f01b60648201526084016102b3565b6004816004811115610bcf57610bcf611480565b0361062a5760405162461bcd60e51b815260206004820152602260248201527f45434453413a20696e76616c6964207369676e6174757265202776272076616c604482015261756560f01b60648201526084016102b3565b60606000610c3683600261137e565b610c41906002611496565b67ffffffffffffffff811115610c5957610c5961108d565b6040519080825280601f01601f191660200182016040528015610c83576020820181803683370190505b509050600360fc1b81600081518110610c9e57610c9e6114a9565b60200101906001600160f81b031916908160001a905350600f60fb1b81600181518110610ccd57610ccd6114a9565b60200101906001600160f81b031916908160001a9053506000610cf184600261137e565b610cfc906001611496565b90505b6001811115610d74576f181899199a1a9b1b9c1cb0b131b232b360811b85600f1660108110610d3057610d306114a9565b1a60f81b828281518110610d4657610d466114a9565b60200101906001600160f81b031916908160001a90535060049490941c93610d6d816114bf565b9050610cff565b508315610dc35760405162461bcd60e51b815260206004820181905260248201527f537472696e67733a20686578206c656e67746820696e73756666696369656e7460448201526064016102b3565b9392505050565b6000306001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016148015610e2357507f000000000000000000000000000000000000000000000000000000000000000046145b15610e4d57507f000000000000000000000000000000000000000000000000000000000000000090565b50604080517f00000000000000000000000000000000000000000000000000000000000000006020808301919091527f0000000000000000000000000000000000000000000000000000000000000000828401527f000000000000000000000000000000000000000000000000000000000000000060608301524660808301523060a0808401919091528351808403909101815260c0909201909252805191012090565b6000807f7fffffffffffffffffffffffffffffff5d576e7357a4501ddfe92f46681b20a0831115610f285750600090506003610fd5565b8460ff16601b14158015610f4057508460ff16601c14155b15610f515750600090506004610fd5565b6040805160008082526020820180845289905260ff881692820192909252606081018690526080810185905260019060a0016020604051602081039080840390855afa158015610fa5573d6000803e3d6000fd5b5050604051601f1901519150506001600160a01b038116610fce57600060019250925050610fd5565b9150600090505b94509492505050565b600060208284031215610ff057600080fd5b81356001600160e01b031981168114610dc357600080fd5b60006020828403121561101a57600080fd5b5035919050565b6001600160a01b038116811461062a57600080fd5b803561104181611021565b919050565b6000806040838503121561105957600080fd5b82359150602083013561106b81611021565b809150509250929050565b80356001600160601b038116811461104157600080fd5b634e487b7160e01b600052604160045260246000fd5b600082601f8301126110b457600080fd5b813567ffffffffffffffff808211156110cf576110cf61108d565b604051601f8301601f19908116603f011681019082821181831017156110f7576110f761108d565b8160405283815286602085880101111561111057600080fd5b836020870160208301376000602085830101528094505050505092915050565b60008083601f84011261114257600080fd5b50813567ffffffffffffffff81111561115a57600080fd5b602083019150836020828501011115610a6a57600080fd5b6000806000806000806000806000806101208b8d03121561119257600080fd5b8a35995060208b01356111a481611021565b985060408b01356111b481611021565b97506111c260608c01611076565b96506111d060808c01611036565b95506111de60a08c01611036565b945060c08b0135935060e08b013567ffffffffffffffff8082111561120257600080fd5b61120e8e838f016110a3565b94506101008d013591508082111561122557600080fd5b506112328d828e01611130565b915080935050809150509295989b9194979a5092959850565b60006020828403121561125d57600080fd5b8135610dc381611021565b60005b8381101561128357818101518382015260200161126b565b50506000910152565b600081518084526112a4816020860160208601611268565b601f01601f19169290920160200192915050565b6001600160a01b03868116825285166020820152604081018490526060810183905260a0608082018190526000906107a19083018461128c565b6000806040838503121561130557600080fd5b825161131081611021565b6020939093015192949293505050565b60018060a01b038616815284602082015283604082015260a06060820152600061134d60a083018561128c565b90506001600160601b03831660808301529695505050505050565b634e487b7160e01b600052601160045260246000fd5b808202811582820484141761021757610217611368565b6000826113b257634e487b7160e01b600052601260045260246000fd5b500490565b8181038181111561021757610217611368565b6020808252601490820152734661696c656420746f2073656e64206d6f6e657960601b604082015260600190565b7f416363657373436f6e74726f6c3a206163636f756e7420000000000000000000815260008351611430816017850160208801611268565b7001034b99036b4b9b9b4b733903937b6329607d1b6017918401918201528351611461816028840160208801611268565b01602801949350505050565b602081526000610dc3602083018461128c565b634e487b7160e01b600052602160045260246000fd5b8082018082111561021757610217611368565b634e487b7160e01b600052603260045260246000fd5b6000816114ce576114ce611368565b50600019019056fea2646970667358221220a093a1b32eef2d0d9c9ea06f256a09fe6578ab9aaab963d24c31d930b3c4453664736f6c63430008110033";

    public static final String FUNC_DEFAULT_ADMIN_ROLE = "DEFAULT_ADMIN_ROLE";

    public static final String FUNC_GETROLEADMIN = "getRoleAdmin";

    public static final String FUNC_GRANTROLE = "grantRole";

    public static final String FUNC_HASROLE = "hasRole";

    public static final String FUNC_MINTNFT = "mintNFT";

    public static final String FUNC_RENOUNCEROLE = "renounceRole";

    public static final String FUNC_REVOKEROLE = "revokeRole";

    public static final String FUNC_SETPLATFORMADDRESS = "setPlatFormAddress";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_TRANSFERNFT = "transferNFT";

    public static final Event ROLEADMINCHANGED_EVENT = new Event("RoleAdminChanged",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Bytes32>(true) {
            }, new TypeReference<Bytes32>(true) {
            }));
    ;

    public static final Event ROLEGRANTED_EVENT = new Event("RoleGranted",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Address>(true) {
            }, new TypeReference<Address>(true) {
            }));
    ;

    public static final Event ROLEREVOKED_EVENT = new Event("RoleRevoked",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Address>(true) {
            }, new TypeReference<Address>(true) {
            }));
    ;

    @Deprecated
    protected LazyMintWith712(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected LazyMintWith712(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected LazyMintWith712(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected LazyMintWith712(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<RoleAdminChangedEventResponse> getRoleAdminChangedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(ROLEADMINCHANGED_EVENT, transactionReceipt);
        ArrayList<RoleAdminChangedEventResponse> responses = new ArrayList<RoleAdminChangedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RoleAdminChangedEventResponse typedResponse = new RoleAdminChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.previousAdminRole = (byte[]) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.newAdminRole = (byte[]) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RoleAdminChangedEventResponse> roleAdminChangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RoleAdminChangedEventResponse>() {
            @Override
            public RoleAdminChangedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(ROLEADMINCHANGED_EVENT, log);
                RoleAdminChangedEventResponse typedResponse = new RoleAdminChangedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.previousAdminRole = (byte[]) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.newAdminRole = (byte[]) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RoleAdminChangedEventResponse> roleAdminChangedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ROLEADMINCHANGED_EVENT));
        return roleAdminChangedEventFlowable(filter);
    }

    public List<RoleGrantedEventResponse> getRoleGrantedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(ROLEGRANTED_EVENT, transactionReceipt);
        ArrayList<RoleGrantedEventResponse> responses = new ArrayList<RoleGrantedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RoleGrantedEventResponse typedResponse = new RoleGrantedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RoleGrantedEventResponse> roleGrantedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RoleGrantedEventResponse>() {
            @Override
            public RoleGrantedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(ROLEGRANTED_EVENT, log);
                RoleGrantedEventResponse typedResponse = new RoleGrantedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RoleGrantedEventResponse> roleGrantedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ROLEGRANTED_EVENT));
        return roleGrantedEventFlowable(filter);
    }

    public List<RoleRevokedEventResponse> getRoleRevokedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(ROLEREVOKED_EVENT, transactionReceipt);
        ArrayList<RoleRevokedEventResponse> responses = new ArrayList<RoleRevokedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RoleRevokedEventResponse typedResponse = new RoleRevokedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RoleRevokedEventResponse> roleRevokedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RoleRevokedEventResponse>() {
            @Override
            public RoleRevokedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(ROLEREVOKED_EVENT, log);
                RoleRevokedEventResponse typedResponse = new RoleRevokedEventResponse();
                typedResponse.log = log;
                typedResponse.role = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.account = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.sender = (String) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RoleRevokedEventResponse> roleRevokedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ROLEREVOKED_EVENT));
        return roleRevokedEventFlowable(filter);
    }

    public RemoteFunctionCall<byte[]> DEFAULT_ADMIN_ROLE() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_DEFAULT_ADMIN_ROLE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<byte[]> getRoleAdmin(byte[] role) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETROLEADMIN,
                Arrays.<Type>asList(new Bytes32(role)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> grantRole(byte[] role, String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GRANTROLE,
                Arrays.<Type>asList(new Bytes32(role),
                        new Address(160, account)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> hasRole(byte[] role, String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_HASROLE,
                Arrays.<Type>asList(new Bytes32(role),
                        new Address(160, account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> mintNFT(BigInteger _tokenId, String _contract, String _creator, BigInteger _royaltyFraction, String _seller, String _to, BigInteger _quantity, byte[] _data, byte[] _signature, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MINTNFT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId),
                        new Address(160, _contract),
                        new Address(160, _creator),
                        new org.web3j.abi.datatypes.generated.Uint96(_royaltyFraction),
                        new Address(160, _seller),
                        new Address(160, _to),
                        new org.web3j.abi.datatypes.generated.Uint256(_quantity),
                        new org.web3j.abi.datatypes.DynamicBytes(_data),
                        new org.web3j.abi.datatypes.DynamicBytes(_signature)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }


    public RemoteFunctionCall<TransactionReceipt> transferNFT(BigInteger _tokenId, String _contract, String _creator, BigInteger _royaltyFraction, String _seller, String _to, BigInteger _amount, byte[] _data, byte[] _signature, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFERNFT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_tokenId),
                        new Address(160, _contract),
                        new Address(160, _creator),
                        new org.web3j.abi.datatypes.generated.Uint96(_royaltyFraction),
                        new Address(160, _seller),
                        new Address(160, _to),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.DynamicBytes(_data),
                        new org.web3j.abi.datatypes.DynamicBytes(_signature)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceRole(byte[] role, String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RENOUNCEROLE,
                Arrays.<Type>asList(new Bytes32(role),
                        new Address(160, account)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revokeRole(byte[] role, String account) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REVOKEROLE,
                Arrays.<Type>asList(new Bytes32(role),
                        new Address(160, account)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPlatFormAddress(String _platformAddress) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETPLATFORMADDRESS,
                Arrays.<Type>asList(new Address(160, _platformAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SUPPORTSINTERFACE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }


    @Deprecated
    public static LazyMintWith712 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new LazyMintWith712(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static LazyMintWith712 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new LazyMintWith712(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static LazyMintWith712 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new LazyMintWith712(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static LazyMintWith712 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new LazyMintWith712(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<LazyMintWith712> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String name, String _nftAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
                new Address(160, _nftAddress)));
        return deployRemoteCall(LazyMintWith712.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<LazyMintWith712> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String name, String _nftAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
                new Address(160, _nftAddress)));
        return deployRemoteCall(LazyMintWith712.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<LazyMintWith712> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String name, String _nftAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
                new Address(160, _nftAddress)));
        return deployRemoteCall(LazyMintWith712.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<LazyMintWith712> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String name, String _nftAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
                new Address(160, _nftAddress)));
        return deployRemoteCall(LazyMintWith712.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class RoleAdminChangedEventResponse extends BaseEventResponse {
        public byte[] role;

        public byte[] previousAdminRole;

        public byte[] newAdminRole;
    }

    public static class RoleGrantedEventResponse extends BaseEventResponse {
        public byte[] role;

        public String account;

        public String sender;
    }

    public static class RoleRevokedEventResponse extends BaseEventResponse {
        public byte[] role;

        public String account;

        public String sender;
    }
}
