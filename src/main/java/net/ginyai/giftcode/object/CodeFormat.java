package net.ginyai.giftcode.object;

import com.google.common.collect.ImmutableList;
import net.ginyai.giftcode.GiftCodePlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeFormat {

    private static final Pattern PATTERN = Pattern.compile("(.*?)\\{(.*?):([0-9]+)}");

    private String formatString;
    private List<Object> format;

    public CodeFormat(String formatString){
        this.formatString = formatString;
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        Matcher matcher = PATTERN.matcher(formatString);
        int i = 0;
        while (matcher.find(i)){
            String before = matcher.group(1);
            String charSet = GiftCodePlugin.getPlugin().getConfig()
                    .getCharSet(matcher.group(2));
            int length = Integer.parseInt(matcher.group(3));
            if(charSet==null){
                builder.add(matcher.group());
            }else {
                if(!before.isEmpty()){
                    builder.add(before);
                }
                builder.add(new RandomPart(charSet,length));
            }
            i = matcher.end(3)+1;
        }
        if(i<formatString.length()){
            builder.add(formatString.substring(i,formatString.length()));
        }
        format = builder.build();
    }

    public String getFormatString() {
        return formatString;
    }

    public String genCode(){
        StringBuilder builder = new StringBuilder();
        for(Object o:format){
            if(o instanceof RandomPart){
                builder.append(((RandomPart) o).gen());
            }else {
                builder.append(o.toString());
            }
        }
        return builder.toString();
    }

    private static class RandomPart{
        private String charSet;
        private int length;

        private RandomPart(String charSet, int length) {
            this.charSet = charSet;
            this.length = length;
        }

        public String gen(){
            StringBuilder out = new StringBuilder();
            for(int i=0;i<length;i++){
                out.append(charSet.charAt((int) (Math.random()*charSet.length())));
            }
            return out.toString();
        }
    }
}
