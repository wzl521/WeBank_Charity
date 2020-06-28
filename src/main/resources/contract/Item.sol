pragma solidity ^0.4.25;

import "./Table.sol";

contract Item {
   
    constructor() public {
        // 构造函数中创建t_item表
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

/*
    描述 :发起项目
    参数 ： 
            item_id: 项目ID
            publisher_name : 发起人
            item_name  : 项目名称 
            beneficiary_name : 受益人 
            target_amount : 目标金额 
            description : 描述 
            
            
    返回值：
            0  项目注册成功
            -1 项目id已存在
            -2 其他错误
    */
    function registerItem(string item_id, string publisher_name, string item_name, string beneficiary_name,
     int target_amount, string description) public returns(int256){
         
        int256 ret_code = 0;
        Table table = openTable();
    
        // 查询项目是否存在
        Condition condition = table.newCondition();
        Entries entries = table.select(item_id, condition);
        int num = entries.size();
        if(num == 0) {
            Entry entry = table.newEntry();
            
            entry.set("item_id", item_id);
            entry.set("item_name", item_name);
            entry.set("publisher_name", publisher_name);
            entry.set("beneficiary_name", beneficiary_name);
            entry.set("target_amount", target_amount);
            entry.set("description", description);
            entry.set("donation_amount", uint256(0));
            entry.set("num_of_donation", uint256(0));
            entry.set("status", "available");
            // 插入
            int count = table.insert(item_id, entry);
            if (count == 1) {
                // 成功
                ret_code = 0;
            } else {
                // 失败? 无权限或者其他错误
                ret_code = -2;
            }
        } else {
            // 项目已存在
            ret_code = -1;
        }
       
        return ret_code;
    }


/*
    描述 : 修改项目信息
    参数 : 
            item_id（不可修改）: 项目ID
            publisher_name : 发起人
            item_name  : 项目名称 
            beneficiary_name : 受益人 
            target_amount : 目标金额 
            description : 描述 
            donation_amount（不可修改）:捐款金额
            num_of_donation（不可修改）:捐款次数 
            status（不可修改）:项目状态
            
    返回值：
            0  修改成功
            其他值 修改失败
    */
    function updateItem(string item_id, string publisher_name, string item_name, string beneficiary_name,
     int target_amount, string description, int donation_amount,int num_of_donation, string status) public returns(int256){
         
        Table table = openTable();
        Entry entry = table.newEntry();
        entry.set("item_name", item_name);
        entry.set("publisher_name", publisher_name);
        entry.set("beneficiary_name", beneficiary_name);
        entry.set("target_amount", target_amount);
        entry.set("description", description);

        Condition condition = table.newCondition();
        condition.EQ("item_id", item_id);
        condition.EQ("donation_amount", donation_amount);
        condition.EQ("num_of_donation", num_of_donation);
        condition.EQ("status", status);
        //更新
        int count = table.update(item_id, entry, condition);

        return count;
    }


    /*
    描述 : 根据item_id获得项目
    参数 ： 
            item_id : 项目ID

    返回值：0:查询成功   -1:查询失败
            publisher_name : 发起人
            item_name  : 项目名称 
            beneficiary_name : 受益人 
            target_amount : 目标金额 
            
            
    */
    function getItem0(string item_id) public constant returns(int256, string, string, string, int) {
        // 打开表
        Table table = openTable();
        // 查询
        Entries entries = table.select(item_id, table.newCondition());
        
        string memory publisher_name = "null";
         string memory item_name = "null";
        string memory beneficiary_name = "null";
        int target_amount = 0;
        
        
        if (0 == uint256(entries.size())) {
            return (-1, publisher_name, item_name,  beneficiary_name, target_amount);
        } else {
            Entry entry = entries.get(0);
            
            publisher_name = entry.getString("publisher_name");
            item_name = entry.getString("item_name");
            beneficiary_name = entry.getString("beneficiary_name");
            target_amount = entry.getInt("target_amount");
           
            return (0, publisher_name, item_name,  beneficiary_name, target_amount);
        }
    }
    
    
    /*
    描述 : 根据item_id获得项目
    参数 ： 
            item_id : 项目ID

    返回值：0:查询成功   -1:查询失败
            description : 描述 
            donation_amount:捐款金额
            num_of_donation:捐款次数 
            status:项目状态
    */
    function getItem1(string item_id) public constant returns(int256, string, int, int, string) {
        // 打开表
        Table table = openTable();
        // 查询
        Entries entries = table.select(item_id, table.newCondition());
        
        string memory description = "null";
        int donation_amount = 0;
        int num_of_donation = 0;
        string memory status = "null";
        
        if (0 == uint256(entries.size())) {
           return (-1, description, donation_amount, num_of_donation, status);
        } else {
            Entry entry = entries.get(0);
            
             description = entry.getString("description");
            donation_amount = entry.getInt("donation_amount");
            num_of_donation = entry.getInt("num_of_donation");
            status = entry.getString("status");
            return (0, description, donation_amount, num_of_donation, status);
        }
    }

/*
    描述 : 根据item_id,取消项目
    参数 ： 
            item_id : 项目ID

    返回值：0:操作成功   -1:操作失败
    */
    function cancelItem(string item_id) public constant returns(int256) {
     int256 ret_code = 0;
     Table table = openTable();
     
     Entry entry = table.newEntry();
     entry.set("status", "cancelled");
     
     Condition condition = table.newCondition();
     condition.EQ("item_id", item_id);
        
    int count = table.update(item_id, entry, condition);
    return count;
    }
     
/*
    描述 : 根据item_id,上架项目
    参数 ： 
            item_id : 项目ID

    返回值：0:操作成功   -1:操作失败
    */
    function pushItem(string item_id) public constant returns(int256) {
     int256 ret_code = 0;
     Table table = openTable();
     Entries entries = table.select(item_id, table.newCondition());
     if(0 == uint256(entries.size())){
         return -1;
     }else{
         Entry entry = entries.get(0);
         entry.set("status", "available");
         return 0;
     }
    }
}
