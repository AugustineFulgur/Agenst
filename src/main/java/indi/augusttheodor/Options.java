package indi.augusttheodor;

import java.util.HashMap;

public class Options {
    //自建Options 解决String格式无法解析问题
    //JAVA也是逼良为娼 fxxk！

    private HashMap<String,String> map;

    public Options(String args) throws Exception{ //严格按照 -a=xxx -b=xxx传入就不会有问题
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
