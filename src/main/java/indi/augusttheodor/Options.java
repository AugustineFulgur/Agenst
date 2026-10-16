package indi.augusttheodor;

import java.util.HashMap;

public class Options {
    //自建Options 解决String格式无法解析问题
    //JAVA也是逼良为娼 fxxk！

    private HashMap<String,String> map;

    public Options(String args) { //严格按照 -a=xxx -b=xxx传入就不会有问题
        this.map=new HashMap<>();
        String[] a=args.split("\n");
        for(String i:a){
            if(i==null || i.trim().isEmpty()){
                continue;
            }
            String[] cmd=i.split("=",2);
            if(cmd.length<2){
                continue;
            }
            map.put(cmd[0].replace("-",""),cmd[1]);
        }
    }

    public boolean hasOption(String key){
        return this.map.containsKey(key);
    }

    public String getOptionValue(String key){
        return this.map.get(key);
    }

}
