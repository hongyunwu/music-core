package com.autoio.core_sdk.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * 单曲信息
 * Created by wcy on 2015/11/27.
 */
@Entity
public class Music {
    // 歌曲类型:本地/网络
    @Transient
    private Type type;
    // [本地歌曲]歌曲id
    @Id
    private long id;
    // 音乐标题
    private String title;
    // 艺术家
    private String artist;
    // 专辑
    private String album;
    // 持续时间
    private long duration;
    // 音乐路径
    private String path;
    // 专辑封面路径
    private String coverPath;
    // 文件名
    private String fileName;
    // 文件大小
    private long fileSize;
    @Transient
    private boolean isLike;


    public long getAlbumId() {
        return albumId;
    }

    private long albumId;

    @Generated(hash = 1277407979)
    public Music(long id, String title, String artist, String album, long duration,
            String path, String coverPath, String fileName, long fileSize,
            long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.coverPath = coverPath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.albumId = albumId;
    }

    @Generated(hash = 1263212761)
    public Music() {
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }



    public boolean isLike() {

        return isLike;
    }

    public void setLike(boolean like) {

        isLike = like;
    }

    public enum Type {
        LOCAL,
        ONLINE
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Music)) {
            return false;
        }
        return this.getId() == ((Music) o).getId();
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Music{" +
                "type=" + type +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }

    public boolean getIsLike() {
        return this.isLike;
    }

    public void setIsLike(boolean isLike) {
        this.isLike = isLike;
    }
}
