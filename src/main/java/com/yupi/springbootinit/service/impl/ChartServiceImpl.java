package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
* @author 惠普
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-06-27 21:29:27
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Resource
    private ChartMapper chartMapper;

    @Override
    public List viewDataById(Long id) {
        LinkedList<String> list = new LinkedList<>();
        String result = chartMapper.viewDataById(id);
        String[] split = result.split(" ");
        String header = split[0];
        list.add(0,header);
        for (int i = 1; i < split.length; i++) {
            list.add(1, split[i]);
        }
        return list;
    }
}




