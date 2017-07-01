# MicroLibrary_Newest

## 此应用是管理员实现读者借还书的app，包括借书、还书两个功能。

  >借书：
      
      1. 管理员扫描读者出示的借书二维码，调取书籍信息；
      2. 将书籍与实体书进行确认后，生成读者支付押金之后；
      3. 读者支付押金后，为读者办理借书。
      
      
  >还书：
  
      1. 管理员扫描读者出示的还书二维码，调取所借书籍信息，与实体书进行对比；
      2. 确认无误后，退还读者的押金，并为读者办理还书。
      
## 各个模块的介绍：
### activity：
    
      MainActivity.java: 登录界面
      LoginSuccessActivity.java: 登录成功界面
      BorrowActivity.java: 借书界面
      ReturnActivity.java: 还书界面
      PayActivity.java: 支付界面
      
### bean：
      BookDetailInfo.java: 书籍类，负责定义书籍的一些属性。
      
### utils:
      HttpUtils.java: 工具类，主要负责网络请求处理。
