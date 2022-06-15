package szhzz.Config;

/**
 * Created by Administrator on 2015/5/21.
 */
public class FunctionExp {
    protected String funName = null;
    protected String[] args = null;
    protected String express = "";


    public FunctionExp(String exp) {
        decode(exp);
    }

     protected void decode(String exp) {
        if (exp != null) {
            express = exp.replace(" ", "");
            if (express.contains("(")) {
                funName = express.substring(0, express.indexOf("("));
                String params = express.substring(express.indexOf("(") + 1, express.indexOf(")"));
                args = params.split(",");
            } else {
                funName = express;
            }
        }
    }

    /**
     * 程序员临时专用
     *
     * @param insertedArg
     */
    public void insertArg(String insertedArg) {
        if (insertedArg == null) return;

        express = funName + "(" + insertedArg;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                express += ("," + args[i]);
            }
        }
        express += ")";
        decode(express);
    }

    public void removeArg(int index) {
        int argCount = 0;
        express = funName + "(";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (index == i) continue;
                if (argCount > 0) {
                    express += ",";
                }
                argCount++;
                express += args[i];
            }
        }
        express += ")";
        decode(express);
    }

    public int getArgsCount() {
        if (args == null) return 0;
        return args.length;
    }

    public boolean isFunction(String funName) {
        return funName != null && this.funName.equalsIgnoreCase(funName);
    }

    public String getFunName() {
        return funName;
    }

    public String getArg(int i, String defaultVal) {
        if (i < 0 || i >= args.length) return defaultVal;
        if (args[i] == null) return defaultVal;
        return args[i];
    }

    public String[] getArgs() {
        return args;
    }

    public String getArg(int i) throws IllegalArgumentException {
        if (args == null || i < 0 || i >= args.length) {
            throw new IllegalArgumentException("IllegalArgumentException");
        }
        return args[i];
    }

    public String getExpress() {
        return express;
    }

    public Object execute(String flag, Object... args){
        return null;
    }
}
