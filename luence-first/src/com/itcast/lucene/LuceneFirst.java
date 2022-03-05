package com.itcast.lucene;

import com.sun.deploy.security.BadCertificateDialog;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.*;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

public class LuceneFirst {
    //指定索引库保存的位置。
    private String path = "D:\\temp\\index";
    //读取磁盘上的文件
    private String path1 = "D:\\BaiduNetdiskDownload\\12-lucene\\02.参考资料\\searchsource";
    //指定检索的内容
    private String searchName = "spring";

    @Test
    public void createIndex() throws Exception{
        File file = new File(path);
        for (File file1: file.listFiles()){
            System.out.println(file1);
            if (file1.isFile()&& file1.exists()){
                file1.delete();
            }
        }
//        1、创建一个Director对象，指定索引库保存的位置。
        Directory directory = FSDirectory.open(new File(path).toPath());
//        2、基于Directory对象创建一个IndexWriter对象
        IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);
//        3、读取磁盘上的文件，对应每个文件创建一个文档对象。
        File dir = new File(path1);
        File[] files = dir.listFiles();
        for (File f: files){
            String fileName = f.getName();
            String filePath = f.getPath();
            String fileContent = FileUtils.readFileToString(f,"UTF-8");
            long fileSize = FileUtils.sizeOf(f);

            //创建Field
            //参数1：域的名称，参数2：域的内容，参数3：是否存储
            Field fieldName = new TextField("name", fileName, Field.Store.YES);
            //Field fieldPath = new TextField("path", filePath, Field.Store.YES);
            Field fieldPath = new StoredField("path", filePath);
            Field fieldContent = new TextField("content", fileContent, Field.Store.YES);
            //Field fieldSize = new TextField("size", fileSize + "", Field.Store.YES);
            Field fieldSizeValue = new LongPoint("size", fileSize);
            Field fieldSizeStore = new StoredField("size", fileSize);
            //创建文档对象
            Document document = new Document();
            //向文档对象中添加域
            document.add(fieldName);
            document.add(fieldPath);
            document.add(fieldContent);
            //document.add(fieldSize);
            document.add(fieldSizeValue);
            document.add(fieldSizeStore);
//        5、把文档对象写入索引库
            indexWriter.addDocument(document);
        }
//        6、关闭indexwriter对象
        indexWriter.close();
    }

    @Test
    public void searchIndex() throws Exception{
//        1、创建一个Director对象，指定索引库的位置
        Directory directory = FSDirectory.open(new File(path).toPath());
//        2、创建一个IndexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
//        3、创建一个IndexSearcher对象，构造方法中的参数indexReader对象。
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//        4、创建一个Query对象，TermQuery
        Query query = new TermQuery(new Term("content",searchName));
//        5、执行查询，得到一个TopDocs对象
        TopDocs topDocs = indexSearcher.search(query,10);
//        6、取查询结果的总记录数
        System.out.println("查询总记录数："+topDocs.totalHits);
//        7、取文档列表
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//        8、打印文档中的内容
        for (ScoreDoc doc: scoreDocs){
            int docId = doc.doc;
            Document document = indexSearcher.doc(docId);
            System.out.println(document.get("name"));
            System.out.println(document.get("path"));
            System.out.println(document.get("size"));
            System.out.println(document.get("content"));
            System.out.println("===================================");
        }
//        9、关闭IndexReader对象
        indexReader.close();
    }

    @Test
    public void testTokenStream() throws Exception {
        //1）创建一个Analyzer对象，StandardAnalyzer对象
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();
        //2）使用分析器对象的tokenStream方法获得一个TokenStream对象
        TokenStream tokenStream = analyzer.tokenStream("", "2017年12月14日 - 传智播客Lucene概述公安局Lucene是一款高性能的、可扩展的信息检索(IR)工具库。信息检索是指文档搜索、文档内信息搜索或者文档相关的元数据搜索等操作。");
        //3）向TokenStream对象中设置一个引用，相当于数一个指针
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        //4）调用TokenStream对象的rest方法。如果不调用抛异常
        tokenStream.reset();
        //5）使用while循环遍历TokenStream对象
        while(tokenStream.incrementToken()) {
            System.out.println(charTermAttribute.toString());
        }
        //6）关闭TokenStream对象
        tokenStream.close();
    }

}
