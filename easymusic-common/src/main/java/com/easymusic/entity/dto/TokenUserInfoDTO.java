package com.easymusic.entity.dto;

import lombok.Data;

/**
 * @author cyt
 * * @date 2025/11/19 17:18:29
 */

@Data
public class TokenUserInfoDTO {

    private String userId;
    private String token;
    private String nickName;
    private String avatar;
    private Integer integral;
}
