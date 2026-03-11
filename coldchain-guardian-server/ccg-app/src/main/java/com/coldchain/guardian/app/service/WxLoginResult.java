package com.coldchain.guardian.app.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// 🌟 修复点 1：忽略掉微信未来可能新增的、我们不需要的额外字段，防止报错
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxLoginResult {

    @JsonProperty("openid")
    private String openid;

    // 🌟 修复点 2：明确告诉解析器，JSON 里的 session_key 对应这个字段
    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("unionid")
    private String unionid;

    @JsonProperty("errcode")
    private Integer errcode;

    @JsonProperty("errmsg")
    private String errmsg;
}