pragma solidity ^0.4.25;

contract Charity {
    struct UserEntity {
        string id;
        string kind; //用户类型
        string name; //用户名
        uint256 balance; //用户余额
        string phone;
        string location;
        string email;
        Item[] ownItems; //发起的项目
        Item[] partItems; //参加的项目
    }

    struct Item {
        address owner; //发起人地址
        string ID; //项目ID
        string iname; //项目名称
        string oname; //发起人姓名
        uint256 amount; //已筹金额
        string describe; //项目详情
        uint256 target; //目标金额
        bool isvalid; //有效状态
    }

    struct Record {
        address user; //捐献人
        address owner; //发起人
        uint256 rid; //记录编号
        uint256 money; //捐款金额
        Item item; //捐款的项目
    }

    mapping(address => UserEntity) account;
    Item[] items;
    mapping(uint256 => Record) records;
    uint256 randNonce;

    constructor() {
        randNonce = 0;
    }

    //用户注册
    function registerUser(
        string memory _name,
        string memory _phone,
        string memory _location,
        string memory _email
    ) public {
        account[msg.sender].name = _name;
        account[msg.sender].phone = _phone;
        account[msg.sender].kind = "User";
        account[msg.sender].balance = 1000;
        account[msg.sender].location = _location;
        account[msg.sender].email = _email;
    }

    //发起项目
    function registerItem(
        string memory _ID,
        string memory _iname,
        string memory _describe,
        uint256 _target
    ) public returns (uint256) {
        items.push(
            Item(
                msg.sender,
                _ID,
                _iname,
                account[msg.sender].name,
                0,
                _describe,
                _target,
                true
            )
        );
        account[msg.sender].ownItems.push(
            Item(
                msg.sender,
                _ID,
                _iname,
                account[msg.sender].name,
                0,
                _describe,
                _target,
                true
            )
        );
        return items.length - 1;
    }

    //捐款
    function donate(uint256 _money, uint256 _numb) public returns (uint256) {
        Item memory _item = items[_numb];
        address _to = _item.owner;
        address donator = msg.sender;
        if (_money > 0 && _money < account[donator].balance) {
            account[donator].balance -= _money;
            account[_to].balance += _money;
            _item.amount += _money;
            uint256 _rid = uint256(keccak256(now, msg.sender, randNonce));
            randNonce++;
            records[_rid] = Record(donator, _to, _rid, _money, _item);
            account[donator].partItems.push(_item);
            return _rid;
        }
    }

    //撤销捐款
    function undoDonate(uint256 _rid) public {
        Record memory _record = records[_rid];
        address _from = _record.user;
        address _to = _record.owner;
        uint256 _money = _record.money;
        Item memory _item = _record.item;
        account[_from].balance += _money;
        account[_to].balance -= _money;
        _item.amount -= _money;
        delete records[_rid];
    }

    //取消项目
    function cancelItem(uint256 _numb) public view {
        Item memory _item = items[_numb];
        _item.isvalid = false;
    }

    //获得项目基本信息（由于solidity只支持7个返回值，因此拆分）
    function getItemBase(uint256 numb)
        public
        view
        returns (
            address,
            string memory,
            string memory
        )
    {
        return (items[numb].owner, items[numb].ID, items[numb].iname);
    }

    function getItemDetails(uint256 numb)
        public
        view
        returns (
            string memory,
            uint256,
            string memory,
            uint256,
            bool
        )
    {
        return (
            items[numb].oname,
            items[numb].amount,
            items[numb].describe,
            items[numb].target,
            items[numb].isvalid
        );
    }
}
