package com.ptmj.datum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PtmjDesktopFileDownloadMapper {
    /**
     * 桌面端用户文件下载列表
     *
     * @param userId
     * @return
     * @Author zac
     * @Date 2026/4/8 22:00
     */
    @Select("select distinct file_id from ptmj_file_download where user_id = #{userId} and (remark is null or remark <> '0')")
    List<Integer> selectPtmjDesktopFileDownloadList(Long userId);

    /**
     * 根据用户ID和试卷ID删除下载记录
     *
     * @param userId 用户ID
     * @param fileId 试卷ID
     * @return 影响行数
     * @Author zac
     */
    @Update("update ptmj_file_download set remark = '0', update_time = now() where user_id = #{userId} and file_id = #{fileId} and (remark is null or remark <> '0')")
    int hideByUserIdAndFileId(@Param("userId") Long userId, @Param("fileId") Long fileId);
}
