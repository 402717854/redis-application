package com.redis.application.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class SensitiveWordReq {
    private List<String> sensitiveWordList;
}
