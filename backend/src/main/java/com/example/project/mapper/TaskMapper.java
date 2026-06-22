package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务 Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
