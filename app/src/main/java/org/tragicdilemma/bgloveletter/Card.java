package org.tragicdilemma.bgloveletter;

public class Card {
    private Integer value;
    private static Integer optCard7, optCard8;


    public Card(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static void setOpt(int card7, int card8){
        optCard7 = card7;
        optCard8 = card8;
    }

    public String getDisplayName(){
        String[] names = {"嗶莫[1]", "馬歇爾[2]", "老皮[3]", "腫泡泡公主[4]", "阿寶[5]", "冰霸王[6]", "[7]", "[8]", "陰魔王[X]"};
        String[] names7 = {"艾薇爾[7]", "樹鼻妹[7]"};
        String[] names8 = {"泡泡糖公主[8]", "火焰公主[8]", "檸檬公爵[8]", "彩虹姐姐[8]"};
        if(value == 0)return "[0]";
        if(value == 7)return names7[optCard7];
        if(value == 8)return names8[optCard8];
        return names[value - 1];
    }

    public int getDrawable(){
        switch (value){
            case 1:
                return R.drawable.card_1;
            case 2:
                return R.drawable.card_2;
            case 3:
                return R.drawable.card_3;
            case 4:
                return R.drawable.card_4;
            case 5:
                return R.drawable.card_5;
            case 6:
                return R.drawable.card_6;
            case 7:
                switch (optCard7){
                    case 0:
                        return R.drawable.card_7;
                    case 1:
                        return R.drawable.card_7_2;
                }
                break;
            case 8:
                switch (optCard8){
                    case 0:
                        return R.drawable.card_8;
                    case 1:
                        return R.drawable.card_8_2;
                    case 2:
                        return R.drawable.card_8_3;
                    case 3:
                        return R.drawable.card_8_4;
                }
                break;
            case 9:
                return R.drawable.card_9;
        }
        return R.drawable.card_back;
    }
}
