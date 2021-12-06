package mandarin.packpack.supporter.event;

import common.system.files.VFile;
import common.util.stage.MapColc;
import common.util.stage.StageMap;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.event.group.ContainedGroupHandler;
import mandarin.packpack.supporter.event.group.GroupHandler;
import mandarin.packpack.supporter.event.group.NormalGroupHandler;
import mandarin.packpack.supporter.event.group.SequenceGroupHandler;
import mandarin.packpack.supporter.lang.LangID;

import java.util.*;

public class EventFactor {
    static {
        Calendar c = Calendar.getInstance();

        currentYear = c.get(Calendar.YEAR);
    }

    public static int currentYear;

    public static EventDate END = new EventDate(20300101);
    public static String NOMAX = "99.99.99";

    public static final int EN = 0;
    public static final int ZH = 1;
    public static final int KR = 2;
    public static final int JP = 3;

    public static final int GATYA = 0;
    public static final int ITEM = 1;
    public static final int SALE = 2;

    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 4;
    public static final int WEDNESDAY = 8;
    public static final int THURSDAY = 16;
    public static final int FRIDAY = 32;
    public static final int SATURDAY = 64;
    public static final int WEEKEND = 65;

    public static final int REWARDNORMAL = 0;
    public static final int REWARDUNIT = 1;
    public static final int REWARDTRUE = 2;

    public static final Map<Integer, int[]> missionReward = new HashMap<>();

    public static final GroupHandler CYCLONE = new NormalGroupHandler(
            Arrays.asList(
                    Arrays.asList(1015, 1039, 1066, 1122, 1172, 118, 1189, 1193, 1198, 1247),
                    Arrays.asList(1014, 1016, 1043, 1096, 1157, 1169, 1176, 1187, 1195, 1203)
            ),
            "group_0", false
    );

    public static final GroupHandler BUILDERBLITZ = new NormalGroupHandler(
            Collections.singletonList(
                    Arrays.asList(1150, 1151, 1152)
            ),
            "group_1", false
    );

    public static final GroupHandler XPBLITZ = new NormalGroupHandler(
            Arrays.asList(
                    List.of(1028),
                    List.of(1059),
                    List.of(1124),
                    List.of(1155)
            ),
            "group_2", false
    );

    public static final GroupHandler CATFRUITFESTIVAL = new NormalGroupHandler(
            Collections.singletonList(
                    Arrays.asList(1097, 1098, 1099, 1100, 1101)
            ),
            "group_3", false
    );

    public static final GroupHandler CRAZEDFESTIVAL = new SequenceGroupHandler(
            Arrays.asList(
                    Arrays.asList(1017, 1020, 1023, 1102, 1105, 1108),
                    Arrays.asList(1018, 1021, 1024, 1103, 1106, 1109),
                    Arrays.asList(1019, 1022, 1025, 1104, 1107, 1110)
            ),
            "group_4", false
    );

    public static final GroupHandler LILFESTIVAL = new SequenceGroupHandler(
            Arrays.asList(
                    Arrays.asList(1130, 1133, 1136),
                    Arrays.asList(1131, 1134, 1137),
                    Arrays.asList(1132, 1135, 1138)
            ),
            "group_5", false
    );

    public static final GroupHandler METALFESTIVAL = new ContainedGroupHandler(
            Arrays.asList(
                    Arrays.asList(1006, 1007),
                    List.of(1078)
            ),
            "group_6", false
    );

    public static final GroupHandler GAMATOTOXP = new NormalGroupHandler(
            List.of(
                    Arrays.asList(5000, 5001, 5002)
            ),
            "group_7", true
    );

    public static final GroupHandler ITEMDISCOUNT = new NormalGroupHandler(
            List.of(
                    Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 51, 700, 701, 702)
            ),
            "group_8", true
    );

    public static final GroupHandler EOCHALFENERGY = new NormalGroupHandler(
            List.of(
                    Arrays.asList(3000, 3001, 3002)
            ),
            "group_9", true
    );

    public static final GroupHandler ITFHALFENERGY = new NormalGroupHandler(
            List.of(
                    Arrays.asList(3003, 3004, 3005)
            ),
            "group_10", true
    );

    public static final GroupHandler COTCHALFENERGY = new NormalGroupHandler(
            List.of(
                    Arrays.asList(3006, 3007, 3008)
            ),
            "group_11", true
    );

    public static final GroupHandler HALFENERGY = new NormalGroupHandler(
            List.of(
                    Arrays.asList(102, 3000, 3001, 3002, 3003, 3004, 3005, 3006, 3007, 3008)
            ),
            "group_12", true
    );

    public static final GroupHandler GAMATOTOGRANDXP = new NormalGroupHandler(
            List.of(
                    Arrays.asList(5008, 5009, 5010)
            ),
            "group_13" , true
    );

    public static final List<GroupHandler> handlers = Arrays.asList(
            CYCLONE, BUILDERBLITZ, XPBLITZ, CATFRUITFESTIVAL, CRAZEDFESTIVAL, LILFESTIVAL, METALFESTIVAL, GAMATOTOXP,
            ITEMDISCOUNT, EOCHALFENERGY, ITFHALFENERGY, COTCHALFENERGY, HALFENERGY, GAMATOTOGRANDXP
    );

    public static final String GACHAURL = "https://ponos.s3.dualstack.ap-northeast-1.amazonaws.com/information/appli/battlecats/gacha/rare_LLL_R_ID_.html";
    public static final String ANNOUNCEURL = "https://nyanko-announcement.ponosgames.com/v1/notices?platform=google&clientVersion=VVVVVV&countryCode=LL&clearedStageJapan=300&clearedStageFuture=300&clearedUniverse=300&clientTime=DDDDDDDDD&timeDifference=1";

    public static void readMissionReward() {
        VFile vf = VFile.get("./org/data/Mission_Data.csv");

        if(vf == null)
            return;

        Queue<String> q = vf.getData().readLine();

        q.poll();

        String line;

        while((line = q.poll()) != null) {
            String[] data = line.split(",");

            if(data.length < 7)
                continue;

            int missionID = StaticStore.safeParseInt(data[0]);

            int[] missionData = new int[] {
                    StaticStore.safeParseInt(data[4]),
                    StaticStore.safeParseInt(data[5]),
                    StaticStore.safeParseInt(data[6])
            };

            missionReward.put(missionID, missionData);
        }
    }

    public static boolean onlyHoldMissions(StageSchedule schedule) {
        if(!schedule.stages.isEmpty())
            return false;

        for(String id : schedule.unknownStages) {
            int realID = StaticStore.safeParseInt(id);

            if((realID < 8000 || realID >= 10000) && (realID < 15000 || realID >= 16000) && (realID < 17000 || realID >= 18000))
                return false;
        }

        return true;
    }

    public static boolean isWeeklyAndMonthlyMission(StageSchedule schedule) {
        if(!schedule.isMission)
            return false;

        for(String id : schedule.unknownStages) {
            int realID = StaticStore.safeParseInt(id);

            if((realID >= 8000 && realID < 9000) || (realID >= 17000 && realID < 18000))
                return true;
        }

        return false;
    }

    public static int stageToInteger(StageMap map) {
        if(map == null || map.id == null)
            return -1;

        MapColc mc = map.getCont();

        if(mc == null)
            return -1;

        if(!StaticStore.isNumeric(mc.getSID()))
            return -1;

        int mcid = StaticStore.safeParseInt(mc.getSID()) * 1000;

        return mcid + map.id.id;
    }

    public static String getMonth(int mon, int lang) {
        switch (mon) {
            case 1:
                return LangID.getStringByID("january", lang);
            case 2:
                return LangID.getStringByID("february", lang);
            case 3:
                return LangID.getStringByID("march", lang);
            case 4:
                return LangID.getStringByID("april", lang);
            case 5:
                return LangID.getStringByID("may", lang);
            case 6:
                return LangID.getStringByID("june", lang);
            case 7:
                return LangID.getStringByID("july", lang);
            case 8:
                return LangID.getStringByID("august", lang);
            case 9:
                return LangID.getStringByID("september", lang);
            case 10:
                return LangID.getStringByID("october", lang);
            case 11:
                return LangID.getStringByID("november", lang);
            case 12:
                return LangID.getStringByID("december", lang);
            default:
                return "Unknown Month "+mon;
        }
    }

    public String getWhichDay(int data, int lang) {
        switch (data) {
            case MONDAY:
                return LangID.getStringByID("monday", lang);
            case TUESDAY:
                return LangID.getStringByID("tuesday", lang);
            case WEDNESDAY:
                return LangID.getStringByID("wednesday", lang);
            case THURSDAY:
                return LangID.getStringByID("thursday", lang);
            case FRIDAY:
                return LangID.getStringByID("friday", lang);
            case SATURDAY:
                return LangID.getStringByID("saturday", lang);
            case SUNDAY:
                return LangID.getStringByID("sunday", lang);
            case WEEKEND:
                return LangID.getStringByID("weekend", lang);
            default:
                return "Unknown day "+data;
        }
    }

    public static String getNumberExtension(int num, int lang) {
        switch (lang) {
            case EN:
                if(num != 11 && num % 10 == 1)
                    return "st";
                else if(num != 12 && num % 10 == 2)
                    return "nd";
                else if(num != 13 && num % 10 == 3)
                    return "rd";
                else
                    return "th";
            case KR:
                return "일";
            case ZH:
            case JP:
                return "日";
            default:
                return "";
        }
    }

    public static String beautifyVersion(String ver) {
        String[] numbers = ver.split("\\.");

        StringBuilder result = new StringBuilder();

        for(int i = 0; i < numbers.length; i++) {
            if(i == 0){
                result.append(numbers[i]).append(".");
            } else if(StaticStore.safeParseInt(numbers[i]) != 0){
                result.append(numbers[i]).append(".");
            } else {
                break;
            }
        }

        return result.substring(0, result.length() - 1);
    }

    public static int getVersionNumber(String ver) {
        String[] numbers = ver.split("\\.");

        int v = 0;

        for(int i = numbers.length - 1; i >= 0; i--) {
            v += StaticStore.safeParseInt(numbers[i]) * Math.pow(10, (numbers.length - 1 - i) * 2);
        }

        return v;
    }

    public static String preventDotDecimal(String result) {
        StringBuilder b = new StringBuilder();

        String[] contents = result.split("\\.");

        for(int i = 0; i < contents.length; i++) {
            if(i == 0) {
                b.append(contents[i]);
            } else {
                if(Character.isDigit(contents[i].charAt(0))) {
                    b.append("\u200B").append(contents[i]);
                } else {
                    b.append(contents[i]);
                }
            }

            if(i < contents.length - 1)
                b.append(".");
        }

        return b.toString();
    }
}
