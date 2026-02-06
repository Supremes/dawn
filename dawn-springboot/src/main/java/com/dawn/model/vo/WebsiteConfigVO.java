package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "网站配置")
public class WebsiteConfigVO {

    @Schema(description = "网站名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "网站作者昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String englishName;

    @Schema(description = "网站作者", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @Schema(description = "网站头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorAvatar;

    @Schema(description = "网站作者介绍", requiredMode = Schema.RequiredMode.REQUIRED)
    private String authorIntro;

    @Schema(description = "网站logo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logo;

    @Schema(description = "多语言", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer multiLanguage;

    @Schema(description = "网站公告", requiredMode = Schema.RequiredMode.REQUIRED)
    private String notice;

    @Schema(description = "网站创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String websiteCreateTime;

    @Schema(description = "网站备案号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String beianNumber;

    @Schema(description = "QQ登录", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qqLogin;

    @Schema(description = "github", requiredMode = Schema.RequiredMode.REQUIRED)
    private String github;

    @Schema(description = "gitee", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gitee;

    @Schema(description = "qq", requiredMode = Schema.RequiredMode.REQUIRED)
    private String qq;

    @Schema(description = "微信", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weChat;

    @Schema(description = "微博", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weibo;

    @Schema(description = "csdn", requiredMode = Schema.RequiredMode.REQUIRED)
    private String csdn;

    @Schema(description = "zhihu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String zhihu;

    @Schema(description = "juejin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String juejin;

    @Schema(description = "twitter", requiredMode = Schema.RequiredMode.REQUIRED)
    private String twitter;

    @Schema(description = "stackoverflow", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stackoverflow;

    @Schema(description = "游客头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String touristAvatar;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAvatar;

    @Schema(description = "是否评论审核", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isCommentReview;

    @Schema(description = "是否邮箱通知", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isEmailNotice;

    @Schema(description = "是否打赏", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isReward;

    @Schema(description = "微信二维码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weiXinQRCode;

    @Schema(description = "支付宝二维码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String alipayQRCode;
}
