package com.doubleruis.baiduspeech.helper;

/**
 * Created by mac on 2018/10/16.
 */

public class MyAppApiConfig {
    // Config
    public static final int MAX_ATTEMPTS = 5;
    //public static final String PRO_SERVER_BASE_URL = "http://192.168.18.165:8082/";//韩文瑞
    //public static final String PRO_SERVER_BASE_URL = "http://192.168.18.115:8081/";//成海龙
    //public static final String PRO_SERVER_BASE_URL = "http://180.76.140.22:8070/";
    public static final String PRO_SERVER_BASE_URL = "";


    //public static final String HFAPI = "http://mertest.chinapnr.com/muser/publicRequests";
    public static final String HFAPI = "http://cgtest.fuiou.com:8090/control.action";
    //public static final String FILES = "http://abc.hongxinsoft.cn/";
    //public static final String FILES = "http://180.76.140.22/";
    public static final String FILES = "http://image.hujingsuo.com.cn/";

    // URL
    /**用户相关***/
    public static final String REGISTER = "HxWebuser/register.shtml";//注册
    public static final String LOGIN = "HxWebuser/userlogin.shtml";
    public static final String FINDHXWEBUSERBYID = "HxWebuser/findHxWebuserById.shtml";
    public static final String DELETEREDIS = "HxWebuser/deleteredis.shtml";
    public static final String UPDATEUSERMP = "HxWebuser/updateUsermp.shtml";//修改手机号
    public static final String SAVEHXSMSRECORD = "HxSmsRecord/saveHxSmsRecord.shtml";//获取验证码 注册0，修改密码1，交易密码2，绑定银行卡3,修改手机号码4，发送付款验证码5,申请借款18，忘记密码19，指定其他收款人21
    public static final String SIGHTUSRPWD = "HxWebuser/sightUsrpwd.shtml";//忘记密码
    public static final String UPDATEUSRPWD = "HxWebuser/updateUsrpwd.shtml";//修改密码
    public static final String CHECKVERIFICATIONCODE="HxSmsRecord/checkVerificationCode.shtml";//验证码校验
    //public static final String TOREGISTER = "HxWebuser/toRegister.shtml";//用户开户
    public static final String TOREGISTER = "HxWebuserFuyou/regUserByFives.shtml";//用户开户
    public static final String FINDACCOUNTBYWEBUSERID = "HxWebuser/findAccountByWebUserId.shtml";//查询个人账户
    public static final String UPLOADIMAGE = "uploadimage/uploadimage.shtml";//上传图片 post
    public static final String UPDATEAVATAR ="HxWebuser/updateAvatar.shtml";//用户修改头像 post
    public static final String SAVEHXCOMPANY = "HxCompany/saveHxCompany.shtml";//保存企业认证
    public static final String OPENEMAIL = "HxWebuser/openEmail.shtml";//邮箱验证
    /**用户相关***/
    /**资产相关***/
    public static final String FINDUSRBINDCARDLIST = "HxUsrbindcard/findUsrbindcardList.shtml";//查看绑卡列表
    //public static final String SAVEUSRBINDCARD = "HxUsrbindcard/saveUsrbindcard.shtml";//申请绑卡
    public static final String SAVEUSRBINDCARD = "HxUsrbindcardFuYou/bindCard.shtml";//申请绑卡
    //public static final String DELUSRBINDCARD = "HxUsrbindcard/delUsrbindcard.shtml";//删除绑卡
    public static final String DELUSRBINDCARD = "HxUsrbindcardFuYou/unbindCard.shtml";//删除绑卡
    //public static final String SAVERECHARGE = "HxRechargeHistory/saveRecharge.shtml";//充值
    public static final String SAVERECHARGE = "HxRechargeHistoryFuYou/quickRecharge.shtml";//充值
    //public static final String SAVECASH = "HxTdUsercash/saveCash.shtml";//申请提现
    public static final String SAVECASH = "HxTdUsercashFuYou/withdraw.shtml";//申请提现
    public static final String FINDHXACCOUNTWATERBYWEBUSERID = "HxAccountWater/findHxAccountWaterByWebUserId.shtml";//查看流水记录
    public static final String FINDHXBORROWINGTARGETBYPAGE = "HxBorrowingTarget/findHxBorrowingTargetByPage.shtml";//查看标的列表
    public static final String FINDHXBORROWINGTARGETBYID = "HxBorrowingTarget/findHxBorrowingTargetById.shtml";//查看标的详情

    //public static final String SVAEHXBIDRECORDS = "HxBidRecords/saveHxBidRecords.shtml";//保存投资记录
    public static final String SVAEHXBIDRECORDS = "HxBidRecordsFuYou/saveHxBidRecords.shtml";//保存投资记录
    public static final String FINDHXBIDRECORDSBYPAGE = "HxBidRecords/findHxBidRecordsByPage.shtml";//投资人查看投资列表
    public static final String FINDREPAYMENTBYPAGE =  "HxBorrowingTarget/findRepaymentByPage.shtml";//分页查看我的还款
    public static final String FINDHXREPAYMENT = "HxRepaymentPlan/findHxRepaymentPlanByPage.shtml";//查看我的还款分期
    //public static final String REPAYMENT = "HxRepaymentPlan/repayment.shtml";//借款人提交还款
    public static final String REPAYMENT = "HxRepaymentPlanFuYou/repayment.shtml";//借款人提交还款
    public static final String FINDHXTDSYSTEMMESSAGEBYSTATE = "HxTdSystemMessage/findHxTdSystemMessageByState.shtml";//分页查询站内信
    public static final String FINDUSERBYTELANDUSERNAME = "HxBidRecords/findUserByTelAndUserName.shtml";//指定收款人前的查询
    public static final String SAVEHXDESIGNATEDPAYEE = "HxDesignatedPayee/saveHxDesignatedPayee.shtml";//确认指定收款人
    public static final String FINDHXTDSYSTEMMESSAGEBYID = "HxTdSystemMessage/findHxTdSystemMessageById.shtml";//站内信详情
    public static final String AGREEORREFUSEDESIGNATE = "HxTdSystemMessage/agreeOrRefuseDesignate.shtml";//接受/拒绝指定

    public static final String FINDHXTDINVITATIONBYPAGE = "HxTdInvitation/findHxTdInvitationByPage.shtml";//分页查看邀请记录
    public static final String SAVEHXLOANRECORDBYAGENT = "HxLoanRecord/saveHxLoanRecordByAgent.shtml";//经纪人推荐借款申请
    public static final String FINDHXLOANRECORDBYAGENT = "HxLoanRecord/findHxLoanRecordByAgent.shtml";//经纪人查询推荐过的项目
    /**资产相关***/
    /**网站管理***/
    public static final String FINDWEBTYPELIST = "HxWebtype/findWebtypeList.shtml";//查询网站分类列表
    public static final String FINDHXASSESSMENTTOPICBYPAGE = "HxAssessmentTopic/findAllHxAssessmentTopic.shtml";//查看测评问题列表
    public static final String SAVEASSESSMENTNUM = "HxAssessmentTopic/saveAssessmentNum.shtml";//保存评测分数
    public static final String FINDALLHXADTYPEBYAPP = "HxAdType/findAllHxAdTypeByApp.shtml";//查询APP广告位列表
    public static final String FINDHXADBYADTYPEID = "HxAd/findHxAdByHxAdTypeId.shtml";//分页查询APP广告信息
    public static final String FINDALLHXLISTINGTYPE = "HxListingType/findAllHxListingType.shtml";//查看所有挂牌信息类型
    public static final String FINDHXLISTINGINFOBUYID = "HxListingInfo/findHxListingInfoById.shtml";//查看挂牌详情
    public static final String FINDHXLISTINGINFOBYLISTING = "HxListingInfo/findHxListingInfoByListingTypeId.shtml";//根据挂牌信息类型ID查询挂牌信息
    public static final String FINDHXTDLINKSBYPAGE = "HxTdLinks/findHxTdLinksByPage.shtml";//分页查询友情链接
    public static final String FINDLINKDETAIL = "HxTdLinks/findLinkDetail.shtml";//友情链接详情
    public static final String SAVEHXLOANRECORD = "HxLoanRecord/saveHxLoanRecord.shtml";//保存借款申请记录
    public static final String FINDHXTDNOTICEBYPAGE = "HxTdNotice/findHxTdNoticeByPage.shtml";//查询公告
    public static final String FINDAGREEMENT = "HxTdAboutNews/findAgreement.shtml";//查询互金服网站协议
    /**网站管理***/
}