package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;

import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiMarketDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class HuobiWSMarketDetailResp extends HuobiWSResp {

    public HuobiMarketDetail tick;

    public HuobiWSMarketDetailResp() {

    }
}
