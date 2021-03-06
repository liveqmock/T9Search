package com.carl.t9search.framework.app;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import com.carl.t9search.App;
import com.carl.t9search.R;
import com.carl.t9search.utils.Logger;
import com.carl.t9search.utils.PinYinUtils;

@Table("app_info")
public class AppInfo extends Model {
    
    Logger log = new Logger(AppInfo.class);
    
    @AutoIncrementPrimaryKey
    @Column("id")
    public long id;
    
    @Column("title")
    public String title;
    
    /**
     * 废弃
     */
    @Column("icon")
    @Deprecated
    Bitmap icon;
    
    @Column("package_name")
    public String packageName;
    
    @Column("intent")
    public Intent intent;
    
    @Column("count")
    public long count;
    
    @Column("keyword_quanpin")
    public String keyword_quanpin;
    
    @Column("keyword_quanpin_t9")
    public String keyword_quanpin_t9;

    @Column("keyword_shouzimu")
    public String keyword_shouzimu;

    @Column("keyword_shouzimu_t9")
    public String keyword_shouzimu_t9;
    
    
    @Column("installed")
    public boolean installed = true;

    private static final Executor sExecutor = Executors.newFixedThreadPool(5);
    private static final PackageManager pkgManager = App.getContext().getPackageManager();
    
    public static AppInfo newInstance(ResolveInfo info){
        PackageManager pkgManager = App.getContext().getPackageManager();
        AppInfo app = new AppInfo();
        app.packageName = info.activityInfo.packageName;
        app.title = info.loadLabel(pkgManager).toString();
//        app.icon = drawableToBitmap(info.loadIcon(pkgManager));
        app.intent = getLaunchIntent(info);
        app.packageName = info.activityInfo.packageName;
        app.setQuanpin(PinYinUtils.string2PinYin(app.title));
        return app;
    }
    
    private static Intent getLaunchIntent(ResolveInfo reInfo){
        if(reInfo == null){
            return null;
        }
        String packageName = reInfo.activityInfo.packageName;
        String name = reInfo.activityInfo.name;
        ComponentName cn = new ComponentName(packageName,name);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        int w = App.getContext().getResources().getDimensionPixelSize(R.dimen.search_item_icon_size);
        int h = w;  
  
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565;  
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);  
        Canvas canvas = new Canvas(bitmap);  
        drawable.setBounds(0, 0, w, h);  
        drawable.draw(canvas);  
        return bitmap;  
    } 
    
    public Bitmap getIcon(){
        try {
            Drawable d = pkgManager.getActivityIcon(intent);
            return drawableToBitmap(d);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean equals(AppInfo appInfo) {
        return intent!= null && intent.toUri(0).equals(appInfo.intent.toUri(0))
                && !TextUtils.isEmpty(packageName) && packageName.equals(appInfo.packageName);
    }
    
    public void setQuanpin(List<String> pinyins){
        StringBuilder sb = new StringBuilder();
        for(String pinyin : pinyins){
            sb.append(pinyin);
            sb.append(",");
        }
        keyword_quanpin = sb.toString();
        keyword_quanpin_t9 = PinYinUtils.pinyin2T9(keyword_quanpin);
    }
    
    public Intent getIntent(){
        return intent;
    }
    
    public void merge(AppInfo appInfo){
        if(appInfo == null){
            return;
        }
        title = appInfo.title;
        keyword_quanpin = appInfo.keyword_quanpin;
        keyword_shouzimu = appInfo.keyword_shouzimu;
        keyword_quanpin_t9 = appInfo.keyword_quanpin_t9;
        keyword_shouzimu_t9 = appInfo.keyword_shouzimu_t9;
        installed = appInfo.installed;
    }
    
    public void loadIcon(ImageView imgView){
        new LoadImgTask().executeOnExecutor(sExecutor, imgView);
    }

    
    private class LoadImgTask extends AsyncTask<ImageView, Object, Drawable>{
        ImageView imageView;
        @Override
        protected Drawable doInBackground(ImageView... params) {
            imageView = params[0];
            try {
                Drawable d = pkgManager.getActivityIcon(intent);
                return d;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            imageView.setImageDrawable(result);
        }
        
        
    }
}
