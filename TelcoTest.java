import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class TelcoTest {

    enum config { sftp_host, sftp_port, sftp_user, sftp_password, sftp_remote_dir, 
        local_dir, sql_user, sql_password, sql_database, sql_url, sql_table }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) System.out.println("Аргументов нет");

        Properties props = new Properties();
		props.load(new FileInputStream(new File(args[0])));
        Connection conn = ManagerBD.connectBD(props.getProperty(config.sql_url.toString()), props.getProperty(config.sql_user.toString()), props.getProperty(config.sql_password.toString()), props.getProperty(config.sql_database.toString()));
        ManagerBD.createTable(conn, props.getProperty(config.sql_table.toString()));

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession(props.getProperty(config.sftp_user.toString()),props.getProperty(config.sftp_host.toString()),Integer.parseInt(props.getProperty(config.sftp_port.toString())));
            session.setPassword(props.getProperty(config.sftp_password.toString()));
            Properties sftp_config = new Properties(); 
            sftp_config.put("StrictHostKeyChecking", "no");
            session.setConfig(sftp_config);
            session.connect();

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.cd(props.getProperty(config.sftp_remote_dir.toString()));
            @SuppressWarnings(value = "unchecked") Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*");
            for(ChannelSftp.LsEntry entry : list) {
                sftpChannel.get( entry.getFilename(), props.getProperty(config.local_dir.toString()) + "/" + entry.getFilename());
                ManagerBD.addElement(conn, props.getProperty(config.sql_table.toString()), entry.getFilename(), formatSize(entry.getAttrs().getSize()), new Date().toString());
            }   

            ManagerBD.outputTable(conn, props.getProperty(config.sql_table.toString()));

            sftpChannel.disconnect();
            session.disconnect();
            conn.close();
        } catch(Exception ex) { ex.printStackTrace(); }
    }
}