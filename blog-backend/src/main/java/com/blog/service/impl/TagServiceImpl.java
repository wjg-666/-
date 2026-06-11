package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> listOrCreate(List<String> names) {
        List<Tag> result = new ArrayList<>();
        if (names == null || names.isEmpty()) return result;
        for (String name : names) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            Tag tag = lambdaQuery().eq(Tag::getName, trimmed).one();
            if (tag == null) {
                tag = new Tag();
                tag.setName(trimmed);
                save(tag);
            }
            result.add(tag);
        }
        return result;
    }
}
