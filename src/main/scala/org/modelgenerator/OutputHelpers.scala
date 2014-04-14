package org.modelgenerator

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

trait OutputHelpers {
  
  def code: String

  def indent(code: String): String

  def writeStringToFile(content: String, folder:String, pkg: String, fileName: String) {
    val folder2 : String = folder + "/" + (pkg.replace(".","/")) + "/"
    new File(folder2).mkdirs()
    val file = new File( folder2+fileName )
    if (!file.exists()) {
      file.createNewFile();
    }
    val fw = new FileWriter(file.getAbsoluteFile());
    val bw = new BufferedWriter(fw);
    bw.write(content);
    bw.close();
  }
  
  def writeToFile(folder:String, pkg: String, fileName: String="Tables.scala") {
    writeStringToFile(packageCode(pkg), folder, pkg, fileName)
  }
  
  def packageCode(pkg : String) : String = {
    s"""
    package ${pkg}

${indent(code)}
    """.trim()
  }
  
}