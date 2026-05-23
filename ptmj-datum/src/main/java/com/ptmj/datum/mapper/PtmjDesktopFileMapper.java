package com.ptmj.datum.mapper;

import com.ptmj.datum.domain.PtmjFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PtmjDesktopFileMapper {

    /**
     * 桌面端用户下载试卷列表
     * @param fileId 试卷id
     * @Author zac
     * @Date 2026/4/8 22:06
     */
    List<PtmjFile> selectPtmjDesktopFileList(@Param("fileId") List<Integer> fileId);
}
