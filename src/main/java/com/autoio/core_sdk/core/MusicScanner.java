package com.autoio.core_sdk.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.autoio.core_sdk.R;
import com.autoio.core_sdk.dao.MusicDao;
import com.autoio.core_sdk.model.Music;
import com.autoio.core_sdk.utils.CoverLoader;
import com.autoio.core_sdk.utils.SystemUtils;
import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * Created by wuhongyun on 17-7-20.
 *
 * 负责扫描根据类型音乐 1.本地音乐,2.USB音乐,3.我最喜欢
 *
 */

public class MusicScanner {

    /**
     * 扫描本地音乐
     * @param context
     * @param musicList
     */
    public static void scanLocaleMusic(Context context, List<Music> musicList){
        musicList.clear();
        //取出数据库中的音频文件
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        int i = 0;
        while (cursor.moveToNext()) {
            // 是否为音乐，魅族手机上始终为0
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (!SystemUtils.isFlyme() && isMusic == 0) {
                continue;
            }

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String unknown = context.getString(R.string.unknown);
            artist = (TextUtils.isEmpty(artist) || artist.toLowerCase().contains("unknown")) ? unknown : artist;
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String coverPath = getCoverPath(context, albumId);
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Music music = new Music();
            music.setId(id);
            music.setAlbumId(albumId);
            music.setType(Music.Type.LOCAL);
            music.setTitle(title);
            music.setArtist(artist);
            music.setAlbum(album);
            music.setDuration(duration);
            music.setPath(path);
            music.setCoverPath(coverPath);
            music.setFileName(fileName);
            music.setFileSize(fileSize);
            List<Music> list = DBManager
                    .getDaoSession(context)
                    .getMusicDao()
                    .queryBuilder()
                    .where(MusicDao.Properties.Id.eq(id))
                    .build()
                    .list();

            if (list!=null&&list.size()>0){
                music.setLike(true);
            }
            if (++i <= 20) {
                // 只加载前20首的缩略图
                CoverLoader.getInstance().loadThumbnail(music);
            }
            musicList.add(music);
        }
        cursor.close();
    }

    /**
     * 获取专辑图片
     *
     * @param context
     * @param albumId
     * @return
     */
    private static String getCoverPath(Context context, long albumId) {

        String path = null;
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + albumId),
                new String[]{"album_art"}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext() && cursor.getColumnCount() > 0) {
                path = cursor.getString(0);
            }
            cursor.close();
        }
        return path;
    }


    /**
     * 扫描usb音乐
     * @param context
     * @param musicList
     */
    public static void scanUSBMusic(Context context,List<Music> musicList){
        String usbPath = AppCache.getUsbPath();
        if (TextUtils.isEmpty(usbPath)) {
            usbPath = "%";
        }else{
            usbPath = usbPath + "%";
        }
        Logger.i("scanUSBMusic->过滤的路径："+usbPath);
        musicList.clear();
        //取出数据库中的音频文件
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null
                , MediaStore.Audio.Media.DATA + " like ?"
                , new String[]{
                        usbPath
                },
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        int i = 0;
        while (cursor.moveToNext()) {
            // 是否为音乐，魅族手机上始终为0
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (!SystemUtils.isFlyme() && isMusic == 0) {
                continue;
            }

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String unknown = context.getString(R.string.unknown);
            artist = (TextUtils.isEmpty(artist) || artist.toLowerCase().contains("unknown")) ? unknown : artist;
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String coverPath = getCoverPath(context, albumId);
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Music music = new Music();
            music.setId(id);
            music.setAlbumId(albumId);
            music.setType(Music.Type.LOCAL);
            music.setTitle(title);
            music.setArtist(artist);
            music.setAlbum(album);
            music.setDuration(duration);
            music.setPath(path);
            music.setCoverPath(coverPath);
            music.setFileName(fileName);
            music.setFileSize(fileSize);
            List<Music> list = DBManager
                    .getDaoSession(context)
                    .getMusicDao()
                    .queryBuilder()
                    .where(MusicDao.Properties.Id.eq(id))
                    .build()
                    .list();

            if (list!=null&&list.size()>0){
                music.setLike(true);
            }
            if (++i <= 20) {
                // 只加载前20首的缩略图
                CoverLoader.getInstance().loadThumbnail(music);
            }
            musicList.add(music);
        }
        cursor.close();
    }

    /**
     * 扫描数据库中的 favorite 音乐
     * @param context 用此作为上下文链接数据库
     * @param musicList 填充集合
     */
    public static void scanFavoriteMusic(Context context,List<Music> musicList){
        musicList.clear();
        List<Music> musics = DBManager
                .getDaoSession(context)
                .getMusicDao()
                .loadAll();
        if (musics!=null&&musics.size()>0){
            for (Music music : musics){
                music.setLike(true);
                musicList.add(music);
            }
        }
    }

    /**
     * 添加到我最喜欢列表中
     * @param context 用于链接数据库dao
     * @param music 被添加的music
     */
    public static void addFavoriteMusic(Context context,Music music) {

        MusicDao musicDao = DBManager
                .getDaoSession(context)
                .getMusicDao();
        List<Music> musicList = musicDao
                .queryBuilder()
                .where(MusicDao.Properties.Id.eq(music.getId()))
                .build()
                .list();
        if (musicList!=null&&musicList.size()>0){
            Music daoMusic = musicList.get(0);
            daoMusic.setIsLike(true);
            daoMusic.setFileName(music.getFileName());
            daoMusic.setCoverPath(music.getCoverPath());
            daoMusic.setAlbum(music.getAlbum());
            daoMusic.setAlbumId(music.getAlbumId());
            daoMusic.setFileSize(music.getFileSize());
            daoMusic.setDuration(music.getDuration());
            //没必要因为设置了like不保存
            musicDao.update(daoMusic);
            Log.i("MusicScanner","musicDao.update->"+music);
        }else{
            musicDao.insert(music);
            Log.i("MusicScanner","musicDao.insert"+music+"...");
        }
        List<Music> favoriteMusicList = AppCache.getFavoriteMusicList();
        if (favoriteMusicList.contains(music)){
            Log.i("MusicScanner","什么鬼，"+music+"已经被添加到我最喜欢列表中啦...");
        }else{
            favoriteMusicList.add(music);
        }
    }

    /**
     * 从我最喜欢列表中移除music
     * @param context 用于链接数据库dao
     * @param music 希望被移除的music
     */
    public static void removeFavoriteMusic(Context context, Music music){
        MusicDao musicDao = DBManager
                .getDaoSession(context)
                .getMusicDao();
        List<Music> musicList = musicDao
                .queryBuilder()
                .where(MusicDao.Properties.Id.eq(music.getId()))
                .build()
                .list();

        if (musicList!=null&&musicList.size()>0){
            Log.i("MusicScanner","musicDao.delete->"+music);
            musicDao.delete(musicList.get(0));
        }else{
            Log.i("MusicScanner","别移除了，数据库中本就没有"+music+"，移啥移...");
        }
        List<Music> favoriteMusicList = AppCache.getFavoriteMusicList();
        if (favoriteMusicList.contains(music)){
            favoriteMusicList.remove(music);
        }else{
            Log.i("MusicScanner","没发现我最喜欢的列表中有："+music);
        }
    }

    public static void removeAllFavorite(Context context){
        DBManager
                .getDaoSession(context)
                .getMusicDao()
                .deleteAll();
        AppCache.getFavoriteMusicList().clear();
    }
}
