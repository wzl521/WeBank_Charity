pragma solidity ^0.4.25;

import "./Table.sol";
contract Charity {
    struct UserEntity{
        string kind;//用户类型
        string name;//用户名
        uint balance;//用户余额
        string phone;
        uint256[] ownItemsId;//发起的项目
        uint256[] partItemsId;//参加的项目
    }

    struct Record {
        address user; //捐献人
        address owner; //发起人
        uint256 rid; //记录编号
        uint256 money; //捐款金额
        uint256 item_id; //捐款的项目
    }

    mapping(address => UserEntity) account;
    mapping(uint256 => Record) records;
    uint256 randNonce;
    uint256[] allItemsId;

    constructor() {
        randNonce = 0;
        createTable();
    }

    function createTable() private {
        TableFactory tf = TableFactory(0x1001);
        // 项目信息表, key : item_id
        tf.createTable("t_item", "item_id",
        "item_name, publisher_name, beneficiary_name,donation_amount, target_amount, num_of_donation,description, status");
    }

    function openTable() private returns(Table) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_item");
        return table;
    }

    event registerUserEvent(address sender);
    event registerItemEvent(int256 ret_code, uint256 id);
    event updateItemEvent();
    event cancelItemEvent(int256 ret_code);
    event pushItemEvent(int256 ret_code);
    event donateEvent(uint256 id);
    event undoDonateEvent();

    //用户注册
    function registerUser(
        string memory _name,
        string memory _phone
    ) public {
        account[msg.sender].name = _name;
        account[msg.sender].phone = _phone;
        account[msg.sender].kind = "User";
        account[msg.sender].balance = 1000;
        emit registerUserEvent(msg.sender);
    }
//返回地址
    function getMsgSender() view returns(address) {
        return msg.sender;
    }

    function registerItem(string item_name, string beneficiary_name,
     int target_amount, string description) public {

        int256 ret_code = 0;
        Table table = openTable();
        uint256 item_id = uint256(keccak256(now, msg.sender, randNonce));
        randNonce++;
        // 查询项目是否存在
        Condition condition = table.newCondition();
        Entries entries = table.select(uint2str(item_id), condition);
        int num = entries.size();
        if(num == 0) {
            Entry entry = table.newEntry();

            entry.set("item_id", uint2str(item_id));
            entry.set("item_name", item_name);
            //entry.set("publisher_address", msg.sender);
            entry.set("publisher_name", account[msg.sender].name);
            entry.set("beneficiary_name", beneficiary_name);
            entry.set("target_amount", target_amount);
            entry.set("description", description);
            entry.set("donation_amount", uint256(0));
            entry.set("num_of_donation", uint256(0));
            entry.set("status", "available");
            // 插入
            int count = table.insert(uint2str(item_id), entry);
            if (count == 1) {
                // 成功
                ret_code = 0;
                account[msg.sender].ownItemsId.push(item_id);
                allItemsId.push(item_id);
            } else {
                // 失败? 无权限或者其他错误
                ret_code = -2;
            }
        } else {
            // 项目已存在
            ret_code = -1;
        }
        emit registerItemEvent(ret_code, item_id);
    }
    function getAllItemsId() public view returns(uint[]){
            return allItemsId;
        }
    function updateItem(uint256 item_id, string item_name, string beneficiary_name,
     uint target_amount, string description, uint256 donation_amount, uint256 num_of_donation) public {

        Table table = openTable();
        Entry entry = table.newEntry();
        entry.set("item_name", item_name);
        entry.set("beneficiary_name", beneficiary_name);
        entry.set("target_amount", target_amount);
        entry.set("description", description);
        entry.set("donation_amount", donation_amount);
        entry.set("num_of_donation", num_of_donation);

        Condition condition = table.newCondition();
        condition.EQ("item_id", uint2str(item_id));

        //更新
        table.update(uint2str(item_id), entry, condition);
        emit updateItemEvent();
    }


    function getItem0(uint256 item_id) public view returns(int256, string, string, string, uint256) {
        // 打开表
        Table table = openTable();
        // 查询
        Entries entries = table.select(uint2str(item_id), table.newCondition());

        string memory publisher_name = "null";
        string memory item_name = "null";
        string memory beneficiary_name = "null";
        uint256 target_amount = 0;


        if (0 == uint256(entries.size())) {
            //emit getItem0Event(-1, publisher_name, item_name,  beneficiary_name, target_amount);
            return (-1, publisher_name, item_name,  beneficiary_name, target_amount);
        } else {
            Entry entry = entries.get(0);

            publisher_name = entry.getString("publisher_name");
            item_name = entry.getString("item_name");
            beneficiary_name = entry.getString("beneficiary_name");
            target_amount = uint256(entry.getUInt("target_amount"));

            //emit getItem0Event(0, publisher_name, item_name,  beneficiary_name, target_amount);
            return (0, publisher_name, item_name,  beneficiary_name, target_amount);
        }
    }

    function getItem1(uint256 item_id) public view returns(int256, string, uint256, uint256, string) {
        // 打开表
        Table table = openTable();
        // 查询
        Entries entries = table.select(uint2str(item_id), table.newCondition());

        string memory description = "null";
        uint256 donation_amount = 0;
        uint256 num_of_donation = 0;
        string memory status = "null";

        if (0 == uint256(entries.size())) {
           //emit getItem1Event(-1, description, donation_amount, num_of_donation, status);
           return (-1, description, donation_amount, num_of_donation, status);
        } else {
            Entry entry = entries.get(0);

            description = entry.getString("description");
            donation_amount = uint256(entry.getUInt("donation_amount"));
            num_of_donation = uint256(entry.getUInt("num_of_donation"));
            status = entry.getString("status");

            //emit getItem1Event(0, description, donation_amount, num_of_donation, status);
            return (0, description, donation_amount, num_of_donation, status);
        }
    }

    function getItemPublisher(uint256 item_id) private view returns(int256, address) {
        Table table = openTable();
        // 查询
        Entries entries = table.select(uint2str(item_id), table.newCondition());

        address addr = msg.sender;

        if (0 == uint256(entries.size())) {
           return (-1, addr);
        } else {
            Entry entry = entries.get(0);

            addr = entry.getAddress("publisher_address");
            return (0, addr);
        }
    }

    function cancelItem(uint256 item_id) public{
        Table table = openTable();

        Entry entry = table.newEntry();
        entry.set("status", "cancelled");

        Condition condition = table.newCondition();
        condition.EQ("item_id", uint2str(item_id));

        int count = table.update(uint2str(item_id), entry, condition);

        emit cancelItemEvent(count);
    }

   //设置用户信息
    function setName(string _name) public{
         account[msg.sender].name = _name;
    }

    function setPhone(string _phone) public{
         account[msg.sender].phone = _phone;
    }

    function setBalance(uint _balance) public{
        account[msg.sender].balance = _balance;
    }

    function setOwnItemsId(uint[] _ownItemsId) public{
         account[msg.sender].ownItemsId = _ownItemsId;
    }

    function setpartItemsId(uint[] _partItemsId) public{
        account[msg.sender].partItemsId = _partItemsId;
    }

    //获取用户信息
    function getName() public view returns(string){
        return account[msg.sender].name;
    }

    function getPhone() public view returns(string){
        return account[msg.sender].phone;
    }

    function getBalance() public view returns(uint){
        return account[msg.sender].balance;
    }

    function getOwnItemsId() public view returns(uint[]){
        return account[msg.sender].ownItemsId;
    }

    function getpartItemsId() public view returns(uint[]){
        return account[msg.sender].partItemsId;
    }

    function getUserInfo() public view returns(string,string,uint,uint[],uint[]){
        return( account[msg.sender].name,
	    account[msg.sender].phone,
	    account[msg.sender].balance,
	    account[msg.sender].ownItemsId,
        account[msg.sender].partItemsId);
    }

     //捐款

    function donate(uint256 _money, uint256 _id) public {
        address donator;
        address _to;
        int256 ret;
        uint256 target_amount;
        uint256 donation_amount;
        uint256 num_of_donation;
        string memory publisher_name;
        string memory item_name;
        string memory beneficiary_name;
        string memory description;
        string memory status;

        (ret, _to) = getItemPublisher(_id);
        if(ret != 0)
            revert("Nonexistent item");
        (ret, publisher_name, item_name, beneficiary_name, target_amount) = getItem0(_id);
        (ret, description, donation_amount, num_of_donation, status) = getItem1(_id);

        if (_money > 0 && _money < account[donator].balance) {
            account[donator].balance -= _money;
            account[_to].balance += _money;
            account[donator].partItemsId.push(_id);
            donation_amount += _money;
            num_of_donation += 1;
            updateItem(_id, item_name, beneficiary_name, target_amount, description, donation_amount, num_of_donation);
            uint256 _rid = uint256(keccak256(now, msg.sender, randNonce));
            randNonce++;
            records[_rid] = Record(donator, _to, _rid, _money, _id);
            emit donateEvent(_rid);
        }

    }

    function undoDonate(uint256 _rid) public {
        Record _record = records[_rid];

        int256 ret;
        uint256 target_amount;
        uint256 donation_amount;
        uint256 num_of_donation;
        string memory publisher_name;
        string memory item_name;
        string memory  beneficiary_name;
        string memory description;
        string memory status;

        (ret, publisher_name, item_name, beneficiary_name, target_amount) = getItem0(_record.item_id);
        (ret, description, donation_amount, num_of_donation, status) = getItem1(_record.item_id);

        account[_record.user].balance += _record.money;
        account[_record.owner].balance -= _record.money;
        donation_amount -= _record.money;
        num_of_donation -= 1;

        updateItem(_record.item_id, item_name, beneficiary_name, target_amount, description, donation_amount, num_of_donation);

        delete records[_rid];
        emit undoDonateEvent();
    }

    function pushItem(uint256 item_id) public view {
        Table table = openTable();
        Entries entries = table.select(uint2str(item_id), table.newCondition());
        if(0 == uint256(entries.size())){
            emit pushItemEvent(-1);
        }else{
            Entry entry = entries.get(0);
            entry.set("status", "available");
            emit pushItemEvent(0);
        }
    }

    function uint2str(uint256 i) internal pure returns (string){
        if (i == 0) return "0";
        uint256 j = i;
        uint256 length;
        while (j != 0){
            length++;
            j /= 10;
        }
        bytes memory bstr = new bytes(length);
        uint256 k = length - 1;
        while (i != 0){
            bstr[k--] = byte(48 + i % 10);
            i /= 10;
        }
        return string(bstr);
    }
}

