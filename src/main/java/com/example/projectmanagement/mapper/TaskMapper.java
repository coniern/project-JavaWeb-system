package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.projectmanagement.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
