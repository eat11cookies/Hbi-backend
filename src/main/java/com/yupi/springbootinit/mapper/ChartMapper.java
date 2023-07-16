package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 惠普
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-06-27 21:29:27
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    String viewDataById(Long id);
}




