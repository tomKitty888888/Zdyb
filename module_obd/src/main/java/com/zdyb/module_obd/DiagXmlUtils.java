package com.zdyb.module_obd;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class DiagXmlUtils {
    public static String createFirmwareDom(String path) {

        org.dom4j.Document document = DocumentHelper.createDocument();
        org.dom4j.Element root = document.addElement("request");

        root.addElement("cmd").addText("UpdateMCU");
        org.dom4j.Element dataDom = root.addElement("data");
        dataDom.addElement("FilePath").addText(path);


        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GB2312");

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    /**
     * 创建xml数据
     *
     * @return
     */
    public static String createEntryDom(String type) {

        org.dom4j.Document document = DocumentHelper.createDocument();
        org.dom4j.Element root = document.addElement("request");

        root.addElement("cmd").addText("StartTest");
        org.dom4j.Element dataDom = root.addElement("data");
        dataDom.addElement("FuelType").addText(type);
        dataDom.addElement("Protocol").addText("0");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GB2312");

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static String createCommDom(String command){
        return createCommDom(command,"00");
    }

    public static String createCommDom(String command,String agreement) {

        org.dom4j.Document document = DocumentHelper.createDocument();
        org.dom4j.Element root = document.addElement("request");

        root.addElement("cmd").addText(command);
        root.addElement("data").addElement("Protocol").addText(agreement);

        if (command.equals("StartTest")){ //诊断必须要的3个参数
            root.addElement("FuelType").addText("");
            root.addElement("SameCar").addText("0");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GB2312");

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

}
