package com.example.zxds.dr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.dr.controller.form.TransferForm;
import com.example.zxds.dr.db.pojo.WalletIncomeEntity;
import com.example.zxds.dr.service.WalletIncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet/incomes")
@Tag(name = "WalletIncomeController", description = "司机钱包web接口")
@RequiredArgsConstructor
@Slf4j
public class WalletIncomeController {
    private final WalletIncomeService walletIncomeService;

    @PostMapping
    @Operation(summary = "创建资金流水（转账）")
    public R transfer(@RequestBody @Valid TransferForm form){
        WalletIncomeEntity entity = BeanUtil.toBean(form, WalletIncomeEntity.class);
        entity.setStatus((byte)3);
        int rows = walletIncomeService.transfer(entity, form.getPassword());
        return R.ok().put("rows",rows);
    }
}
