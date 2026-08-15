package indi.augusttheodor;

import java.util.HashMap;

public class Options {
    //鑷缓Options 瑙ｅ喅String鏍煎紡鏃犳硶瑙ｆ瀽闂
    //JAVA涔熸槸閫艰壇涓哄 fxxk锛?

    private HashMap<String,String> map;

    public Options(String args) throws Exception{ //涓ユ牸鎸夌収 -a=xxx -b=xxx浼犲叆灏变笉浼氭湁闂
        this.map=new HashMap<>();
        String[] a=args.split("\n");
        if(a.length<2){
            return;
        }
        for(String i:a){
            String[] cmd=i.split("=");
            map.put(cmd[0].replace("-",""),cmd[1]);
        }
    }

    public boolean hasOption(String key){
        return this.map.containsKey(key);
    }

    public String getOptionValue(String key){
        System.out.println("key:"+key);
        System.out.println("value:"+this.map.get(key));
        return this.map.get(key);
    }

}
