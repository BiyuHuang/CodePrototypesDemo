package com.wallace.demo.app.utils.ioutils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.GroupFactory;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.apache.parquet.hadoop.ParquetReader.builder;

public class ParquetIOUtils {
  static Logger logger = Logger.getLogger(ParquetIOUtils.class);

  public static void main(String[] args) throws Exception {
    //parquetWriter("test\\parquet-out2", "input.txt");
    parquetReaderV2();
  }


  private static void parquetReaderV2() {
    GroupReadSupport readSupport = new GroupReadSupport();
    ParquetReader.Builder<Group> reader = builder(readSupport, new Path("test\\parquet-out2"));
    try (ParquetReader<Group> build = reader.build()) {
      Group row;
      GroupType schema = null;
      while ((row = build.read()) != null) {
        schema = schema == null ? row.getType() : schema;
        for (Type field : schema.getFields()) {
          primitiveFieldReader(field, row);
        }
        Group timeField = row.getGroup("time", 0);
        //通过下标和字段名称都可以获取
                /*System.out.println(line.getString(0, 0)+"\t"+
            line.getString(1, 0)+"\t"+
            time.getInteger(0, 0)+"\t"+
            time.getString(1, 0)+"\t");*/

        for (Type field : timeField.getType().asGroupType().getFields()) {
          switch (field.getOriginalType()) {
            case INT_64:
              row.getInteger(field.getName(), 0);
              break;
            case DECIMAL:
              row.getDouble(field.getName(), 0);
              break;
            case UTF8:
              row.getString(field.getName(), 0);
              break;
            default:
              break;
          }
        }
        logger.info(row.getString("city", 0) + "\t" +
            row.getString("ip", 0) + "\t" +
            timeField.getInteger("ttl", 0) + "\t" +
            timeField.getString("ttl2", 0) + "\t");

        //System.out.println(line.toString());

      }
    } catch (IOException e) {
      logger.error(e);
    }
    logger.info("读取结束");
  }

  static Long binaryToUnscaledLong(Binary binary) {
    // The underlying `ByteBuffer` implementation is guaranteed to be `HeapByteBuffer`, so here
    // we are using `Binary.toByteBuffer.array()` to steal the underlying byte array without
    // copying it.
    ByteBuffer buffer = binary.toByteBuffer();
    byte[] bytes = buffer.array();
    int start = buffer.arrayOffset() + buffer.position();
    int end = buffer.arrayOffset() + buffer.limit();

    long unscaled = 0L;
    int i = start;

    while (i < end) {
      unscaled = (unscaled << 8) | (bytes[i] & 0xff);
      i += 1;
    }

    int bits = 8 * (end - start);
    unscaled = (unscaled << (64 - bits)) >> (64 - bits);
    return unscaled;
  }

  static Object readValue(Type field, Integer index, Group row) {
    Object value = null;
    if (field.isPrimitive()) {
      switch (field.asPrimitiveType().getPrimitiveTypeName()) {
        case FLOAT:
          value = row.getFloat(field.getName(), 0);
          break;
        case INT32:
          if (field.getOriginalType() == OriginalType.DECIMAL) {
            DecimalMetadata metadata = field.asPrimitiveType().getDecimalMetadata();
            int scale = metadata == null ? 0 : metadata.getScale();
            value = BigDecimal.valueOf(row.getInteger(field.getName(), index), scale);
          } else {
            value = row.getInteger(field.getName(), index);
          }
          break;
        case INT64:
          if (field.getOriginalType() == OriginalType.DECIMAL) {
            DecimalMetadata metadata = field.asPrimitiveType().getDecimalMetadata();
            int scale = metadata == null ? 0 : metadata.getScale();
            value = BigDecimal.valueOf(row.getLong(field.getName(), index), scale);
          } else {
            value = row.getLong(field.getName(), index);
          }
          break;
        case BINARY:
          if (field.getOriginalType() == OriginalType.DECIMAL) {
            DecimalMetadata metadata = field.asPrimitiveType().getDecimalMetadata();
            int scale = metadata == null ? 0 : metadata.getScale();
            value = BigDecimal.valueOf(binaryToUnscaledLong(row.getBinary(field.getName(), index)), scale);
          } else {
            value = row.getLong(field.getName(), index);
          }
          break;
        case DOUBLE:
          value = row.getDouble(field.getName(), index);
          break;
        case BOOLEAN:
          value = row.getBoolean(field.getName(), index);
          break;
        case INT96:
          break;
        case FIXED_LEN_BYTE_ARRAY:
          break;
        default:
          throw new RuntimeException("unknown primitive type: " +
              field.asPrimitiveType().getPrimitiveTypeName());
      }
    } else {
      GroupType fieldGroupType = field.asGroupType();
      Group fieldGroup = row.getGroup(field.getName(), index);
      HashMap<String, Object> groupValue = new HashMap<>();
      for (Type singleField : fieldGroupType.getFields()) {
        Object fieldValue;
        int elementNum = fieldGroup.getFieldRepetitionCount(singleField.getName());
        if (elementNum <= 1) {
          fieldValue = readValue(singleField, 0, fieldGroup);
        } else {
          ArrayList<Object> lsValue = new ArrayList<>(elementNum);
          for (int i = 0; i < elementNum; i++) {
            lsValue.add(readValue(field, i, row));
          }
          fieldValue = lsValue;
        }
        groupValue.put(singleField.getName(), fieldValue);
      }
      value = groupValue;
    }
    return value;
  }

  static Object primitiveFieldReader(Type field, Group row) {
    Object rowValue;
    int elementNum = row.getFieldRepetitionCount(field.getName());
    if (elementNum <= 1) {
      rowValue = readValue(field, 0, row);
    } else {
      ArrayList<Object> allValue = new ArrayList<>(elementNum);
      for (int i = 0; i < elementNum; i++) {
        allValue.add(readValue(field, i, row));
      }
      rowValue = allValue;
    }
    return rowValue;
  }

  //新版本中new ParquetReader()所有构造方法好像都弃用了,用上面的builder去构造对象
  static void parquetReader(String inPath) throws Exception {
    GroupReadSupport readSupport = new GroupReadSupport();
    ParquetReader.Builder<Group> builder = builder(readSupport, new Path(inPath));
    // ParquetReader<Group> reader = new ParquetReader<Group>(new Path(inPath), readSupport);
    Group line = null;
    while ((line = builder.build().read()) != null) {
      System.out.println(line.toString());
    }
    System.out.println("读取结束");

  }

  /**
   * @param outPath 输出Parquet格式
   * @param inPath  输入普通文本文件
   * @throws IOException
   */
  static void parquetWriter(String outPath, String inPath) throws IOException {
    MessageType schema = MessageTypeParser.parseMessageType("message Pair {\n" +
        " required binary city (UTF8);\n" +
        " required binary ip (UTF8);\n" +
        " repeated group time {\n" +
        " required int32 ttl;\n" +
        " required binary ttl2;\n" +
        "}\n" +
        "}");
    GroupFactory factory = new SimpleGroupFactory(schema);
    Path path = new Path(outPath);
    Configuration configuration = new Configuration();
    GroupWriteSupport writeSupport = new GroupWriteSupport();
    GroupWriteSupport.setSchema(schema, configuration);
    ParquetWriter<Group> writer = new ParquetWriter<>(path, configuration, writeSupport);
    //把本地文件读取进去，用来生成parquet格式文件
    BufferedReader br = new BufferedReader(new FileReader(new File(inPath)));
    String line;
    Random r = new Random();
    while ((line = br.readLine()) != null) {
      String[] strs = line.split("\\s+");
      if (strs.length == 2) {
        Group group = factory.newGroup()
            .append("city", strs[0])
            .append("ip", strs[1]);
        Group tmpG = group.addGroup("time");
        tmpG.append("ttl", r.nextInt(9) + 1);
        tmpG.append("ttl2", r.nextInt(9) + "_a");
        writer.write(group);
      }
    }
    System.out.println("write end");
    writer.close();
  }
}
