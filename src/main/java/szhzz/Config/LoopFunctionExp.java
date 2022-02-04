package szhzz.Config;


import szhzz.Utils.NU;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2015/5/21.
 */
public class LoopFunctionExp {
    private String funName = null;
    String[] args = null;
    private String express = "";
    ArgumentLoop argumentLoop = null;
    private Double relay = null;  //TODO 不再需要
    private int presize = 6;

    public LoopFunctionExp(String exp) {
        if (exp != null) {
            express = exp.replace(" ", "");
            if (express.contains("(")) {
                funName = express.substring(0, express.indexOf("("));
                String params = express.substring(express.indexOf("(") + 1, express.indexOf(")"));
                args = params.split(",");
            } else {
                funName = null;
                String params = express;
                args = params.split(",");
            }
            prepareLopTest();
        }
    }

    public boolean nextLoop() {
        return argumentLoop.nextLoop();
    }

    public String getNextExpress() {
        StringBuffer sb = new StringBuffer();
        if (funName == null) {
            sb.append(argumentLoop.getCurrentParm());
        } else {
            sb.append(funName);
            sb.append("(").append(argumentLoop.getCurrentParm()).append(")");
        }
        return sb.toString();
    }

    private void prepareLopTest() {
        ArgumentLoop pramLoop = null;
        int level = 0;
        for (String arg : args) {
            if (pramLoop == null) {
                pramLoop = new ArgumentLoop();
                argumentLoop = pramLoop;
            } else {
                pramLoop = pramLoop.getChild();
            }
            pramLoop.setParm(arg);
        }
    }

//    public void setCurrentParam(String exp) {
//        ArgumentLoop pramLoop = null;
//
//        if (exp != null) {
//            String express = exp.replace(" ", "");
//            if (express.contains("(")) {
//                String params = express.substring(express.indexOf("(") + 1, express.indexOf(")"));
//                String[] args = params.split(",");
//                for (String arg : args) {
//                    if (pramLoop == null) {
//                        pramLoop = argumentLoop;
//                    } else {
//                        pramLoop = pramLoop.getChild();
//                    }
//                    pramLoop.setCurrentParam_(arg);
//                }
//            }
//        }
//    }

    public void setRelay(Double relay) {
        this.relay = presized(relay);
    }

    public void setPresize(int presize) {
        this.presize = presize;
    }


    class ArgumentLoop {
        ArgumentLoop child = null;


        String orgParm = "";

        String[] params = null;
        boolean isArray = false;
        int arrayIndex = 0;
        String currentElement = null;

        Double currentNumber = null;
        Double firstP = null;
        Double lastP = null;
        Double step = null;

        public void setParm(String parm) {
            orgParm = parm;

            if (parm.contains(":")) {
                params = parm.split(":");
                if (params.length < 3) {
                    params = null;
                }
            } else if (parm.contains(";")) {
                params = parm.split(";");
                isArray = true;
            }


            if (!isConstant() && !isArray) {
                firstP = presized(NU.parseDouble(params[0], null));
                lastP = presized(NU.parseDouble(params[1], null));
                step = presized(NU.parseDouble(params[2], null));
                if (isNumberRanger()) {
//                        currentNumber = firstP;
//                        if (level == 0) {
//                            currentNumber -= step;
//                        }
//                        level++;
                } else {
                    params = null; //当作常量
                }
            }
        }

        boolean isNumberRanger() {
            return !(firstP == null || lastP == null || step == null);
        }


        public ArgumentLoop getChild() {
            if (child == null) {
                child = new ArgumentLoop();
            }
            return child;
        }

        public boolean nextLoop() {
            if (isConstant()) {
                if (!hasChild()) return false;
                return child.nextLoop();
            }

            if (isNumberRanger()) {
                if (currentNumber == null) {
                    currentNumber = firstP;
                    if (relay != null) {//可中继
                        if (currentNumber <= relay) {
                            currentNumber = presized(relay + step);

                        }
                    }
                } else {
                    if(currentNumber >= lastP){
                        currentNumber += 2*step;
                    }else{
                        currentNumber = presized(currentNumber + step);
                    }
                }
                if ((currentNumber - lastP) >= step) {//double比较的精度的原因
                    if (!hasChild()) return false;

                    if (child.nextLoop()) {
                        currentNumber = firstP;
                        return true;
                    } else {
                        return false;
                    }
                }
            } else if (isArray) {
                if (currentElement == null) {
                    arrayIndex = 0;
                } else {
                    arrayIndex++;
                }
                if (arrayIndex >= params.length) {
                    if (!hasChild()) return false;

                    if (child.nextLoop()) {
                        arrayIndex = 0;
                    } else {
                        return false;
                    }
                }
                currentElement = params[arrayIndex];
            }
            return true;
        }


        public String getCurrentParm() {
            String p;
            if (isArray) {
                if (currentElement == null) {
                    currentElement = params[0];
                }
                p = currentElement;
            } else if (isNumberRanger()) {
                if (currentNumber == null) {
                    currentNumber = firstP;
                }
                p = currentNumber.toString();
            } else {
                p = orgParm;
            }
            if (child != null) {
                p += "," + child.getCurrentParm();
            }
            return p;
        }

//        private void setCurrentParam_(String arg) {
//            if (isNumberRanger()) {
//                currentNumber = presized(NU.parseDouble(arg, currentNumber));
//            } else if (isArray) {
//                for (int i = 0; i < params.length; i++) {
//                    if (arg.equals(params[i])) {
//                        arrayIndex = i;
//                    }
//                }
//            }
//        }

        public boolean isConstant() {
            return params == null;
        }

        public boolean hasChild() {
            return child != null;
        }
    }

    private double presized(double val){
        return BigDecimal.valueOf(val).setScale(presize, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void main(String[] args) {
        LoopFunctionExp f = new LoopFunctionExp("AMTUN(1:20:1 , -9:9:1 , A;B;C , avg;opt)");

        while (f.nextLoop()) {
            System.out.println(f.getNextExpress());
        }
//        System.out.println("===============================");
//        f.setCurrentParam("AMTUN(C,15,8, avg)");
//        while (f.nextLoop()) {
//            System.out.println(f.getNextExpress());
//        }
    }
}
