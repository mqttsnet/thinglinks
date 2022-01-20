package com.mqttsnet.thinglinks.monitor.service.impl;

import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.monitor.api.domain.MailSet;
import com.mqttsnet.thinglinks.monitor.mapper.MailSetMapper;
import com.mqttsnet.thinglinks.monitor.service.MailSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MailSetServiceImpl implements MailSetService {

    @Autowired
    private MailSetMapper mailSetMapper;

    @Override
    public void save(MailSet MailSet) {
        MailSet.setId(UUID.getUUID());
        MailSet.setCreateTime(DateUtils.getNowTime());
        MailSet.setFromMailName(MailSet.getFromMailName().trim());
        MailSet.setFromPwd(MailSet.getFromPwd().trim());
        MailSet.setToMail(MailSet.getToMail().trim());
        MailSet.setSmtpHost(MailSet.getSmtpHost().trim());
        mailSetMapper.save(MailSet);
    }

    @Override
    public int deleteById(String[] id) {
        return mailSetMapper.deleteById(id);
    }

    @Override
    public List<MailSet> selectMailSetList(MailSet mailSet) {
        return mailSetMapper.selectMailSetList(mailSet);
    }

    @Override
    public int updateById(MailSet MailSet) {
        return mailSetMapper.updateById(MailSet);
    }
}
