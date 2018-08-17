package com.cjie.commons.okex.open.api.task;

import com.cjie.commons.okex.open.api.bean.spot.result.Account;
import com.cjie.commons.okex.open.api.bean.spot.result.Ticker;
import com.cjie.commons.okex.open.api.service.spot.SpotAccountAPIService;
import com.cjie.commons.okex.open.api.utils.WXInfoUtils;
import com.cjie.cryptocurrency.quant.mapper.CurrencyBalanceMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Component
@Slf4j
public class CurrencyService {

    @Autowired
    private MineService mineService;

    @Autowired
    private CurrencyBalanceMapper currencyBalanceMapper;

    @Autowired
    private SpotAccountAPIService spotAccountAPIService;

    public void currency(String site, String baseName, String quotaName) throws Exception {

        //查询余额
        Account baseAccount = getBalance(site, baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();

        Account quotaAccount = getBalance(site, quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();

        Ticker ticker = mineService.getTicker(site, baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());

        BigDecimal hold = new BigDecimal(baseHold).multiply(new BigDecimal(marketPrice).add(new BigDecimal(quotaHold)));

        BigDecimal balance = new BigDecimal(baseBalance).multiply(new BigDecimal(marketPrice)).add(new BigDecimal(quotaBalance));

        String text = site;
        StringBuffer desp = new StringBuffer();
        desp.append("币对")
                .append(baseName.toUpperCase()).append(quotaName.toUpperCase())
                .append("   总").append(quotaName.toUpperCase()).append(":").append(balance.setScale(8, RoundingMode.HALF_UP).toString())
                .append("   现价：").append(marketPrice)
                .append("       -----")
                .append(baseName.toUpperCase())
                .append("   共有:").append(new BigDecimal(baseBalance).setScale(8, RoundingMode.HALF_UP).toString())
                .append("   可用:").append(new BigDecimal(baseAccount.getAvailable()).setScale(8, RoundingMode.HALF_UP).toString())
                .append("   冻结:").append(new BigDecimal(baseHold).setScale(8, RoundingMode.HALF_UP).toString())
                .append("       -----")
                .append(quotaName.toUpperCase())
                .append("   共有:").append(new BigDecimal(quotaBalance).setScale(8, RoundingMode.HALF_UP).toString())
                .append("   可用:").append(new BigDecimal(quotaAccount.getAvailable()).setScale(8, RoundingMode.HALF_UP).toString())
                .append("   冻结:").append(new BigDecimal(quotaHold).setScale(8, RoundingMode.HALF_UP).toString());
        WXInfoUtils.sendInfo(text, desp.toString());

        CurrencyService.log.info("add user currency");
        CurrencyBalance currencyBalance = CurrencyBalance.builder()
                .site(site)
                .currency(quotaName)
                .balance(balance)
                .hold(hold)
                .available(balance.subtract(hold))
                .createTime(new Date())
                .modifyTime(new Date())
                .build();
        currencyBalanceMapper.insert(currencyBalance);


    }

    public Account getBalance(String site, String currency) throws Exception {

        return spotAccountAPIService.getAccountByCurrency(site, currency);
    }

}

