package com.parizene.androididmodifier.xml

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.StringReader
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Singleton
class XmlParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val NAMESPACE_HASHES = "<namespaceHashes />"
    }

    private fun readXml(): String? {
        try {
            val command = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf("su", "-c", "abx2xml /data/system/users/0/settings_ssaid.xml -")
            } else {
                arrayOf("su", "-c", "cat /data/system/users/0/settings_ssaid.xml")
            }

            val process = Runtime.getRuntime().exec(command)
            val result =
                BufferedReader(InputStreamReader(process.inputStream)).use(BufferedReader::readText)
            process.waitFor()
            return result

        } catch (e: Exception) {
            Timber.w(e)
        }

        return null
    }

    fun writeXml(xmlString: String): Boolean {
        val fileLocation = "/data/system/users/0/settings_ssaid.xml"

        try {
            val tempFile = File.createTempFile("temp", ".xml", context.cacheDir).apply {
                writeText(xmlString)
                deleteOnExit()
            }

            val command = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf("su", "-c", "xml2abx ${tempFile.absolutePath} $fileLocation")
            } else {
                arrayOf("su", "-c", "cat ${tempFile.absolutePath} > $fileLocation")
            }

            ProcessBuilder(*command).start().apply {
                waitFor()

                if (exitValue() != 0) {
                    errorStream.bufferedReader().useLines { lines ->
                        lines.forEach { Timber.e("Error: $it") }
                    }
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            Timber.w(e)
            return false
        }
    }

    private fun trimNamespaceHashesFromEnd(xmlString: String): Pair<String, Boolean> {
        val elementPattern = "(\\s*${NAMESPACE_HASHES}\\s*)\\z".toRegex(
            setOf(
                RegexOption.MULTILINE,
                RegexOption.DOT_MATCHES_ALL
            )
        )
        val matchResult = elementPattern.find(xmlString)

        return if (matchResult != null) {
            val trimmedXml = xmlString.removeRange(matchResult.range)
            trimmedXml to true
        } else {
            xmlString to false
        }
    }

    fun parseXml(): List<SettingInfo> {
        val xmlString = readXml() ?: return emptyList()
        val (trimmedXmlString, _) = trimNamespaceHashesFromEnd(xmlString)

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val settingList = mutableListOf<SettingInfo>()

        try {
            val doc = dBuilder.parse(InputSource(StringReader(trimmedXmlString)))
            doc.documentElement.normalize()

            val settingElements = doc.getElementsByTagName("setting")
            for (i in 0 until settingElements.length) {
                val node = settingElements.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val eElement = node as Element
                    val id = eElement.getAttribute("id")
                    val name = eElement.getAttribute("name")
                    val value = eElement.getAttribute("value")
                    val packageName = eElement.getAttribute("package")
                    val defaultValue = eElement.getAttribute("defaultValue")
                    val defaultSysSet = eElement.getAttribute("defaultSysSet").toBoolean()
                    val tag =
                        if (eElement.getAttribute("tag") != "null") eElement.getAttribute("tag") else null

                    settingList.add(
                        SettingInfo(
                            id = id,
                            name = name,
                            value = value,
                            packageName = packageName,
                            defaultValue = defaultValue,
                            defaultSysSet = defaultSysSet,
                            tag = tag
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.w(e)
        }

        return settingList
    }

    private fun is64BitHexString(value: String): Boolean {
        val hexPattern = Regex("^[a-fA-F0-9]{16}$")
        return hexPattern.matches(value)
    }

    fun updateXml(packageName: String, newValue: String): String? {
        if (!is64BitHexString(newValue)) return null
        val xmlString = readXml() ?: return null
        val (trimmedXmlString, hasNamespaceHashes) = trimNamespaceHashesFromEnd(xmlString)

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(InputSource(StringReader(trimmedXmlString)))
            doc.documentElement.normalize()

            val settings = doc.getElementsByTagName("setting")

            for (i in 0 until settings.length) {
                val setting = settings.item(i) as Element
                if (setting.getAttribute("package") == packageName) {
                    setting.setAttribute("value", newValue)
                    setting.setAttribute("defaultValue", newValue)
                }
            }

            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0")
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

            val domSource = DOMSource(doc)
            val writer = StringWriter()
            val result = StreamResult(writer)
            transformer.transform(domSource, result)

            return buildString {
                append(writer.toString())
                if (hasNamespaceHashes) append(NAMESPACE_HASHES)
            }
        } catch (e: Exception) {
            Timber.w(e)
        }

        return null
    }
}