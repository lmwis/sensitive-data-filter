package com.lmwis.datachecker.computer.util;

import com.lmwis.datachecker.common.constant.HttpConstant;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class HttpMessageUtil {
	
	private static final String[] SUPPORT_PARSED_STRING = new String[] {
		"application/javascript", "text/css", "text/html", "application/x-javascript", "text/plain", "application/xml",
		"application/xhtml", "text/javascript", "application/json"
	};

    public static StringBuilder appendRequest(StringBuilder buf, DefaultHttpRequest req) {
		appendInitialLine(buf, req);
		appendHeaders(buf, req.headers());
		removeLastNewLine(buf);
        return buf;
    }

    public static StringBuilder appendRequest(StringBuilder buf, List<? extends DefaultHttpRequest> reqList) {
		for (DefaultHttpRequest req : reqList) {
			appendRequest(buf, req);
			buf.append(StringUtil.NEWLINE).append("====================").append(StringUtil.NEWLINE);
		}
        return buf;
    }
    
	public static StringBuilder appendResponse(StringBuilder buf, FullHttpResponse res) {
		appendInitialLine(buf, res);
		appendHeaders(buf, res.headers());
		removeLastNewLine(buf);
		return buf;
	}
    
    public static StringBuilder appendResponse(StringBuilder buf, List<? extends FullHttpResponse> resList) {
    	for (FullHttpResponse res : resList) {
    		appendResponse(buf, res);
    	    buf.append(StringUtil.NEWLINE).append("====================").append(StringUtil.NEWLINE);
    	}
    	return buf;
    }

    static StringBuilder appendFullRequest(StringBuilder buf, FullHttpRequest req) {
        appendFullCommon(buf, req);
        appendInitialLine(buf, req);
        appendHeaders(buf, req.headers());
        appendHeaders(buf, req.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    static StringBuilder appendFullResponse(StringBuilder buf, FullHttpResponse res) {
        appendFullCommon(buf, res);
        appendInitialLine(buf, res);
        appendHeaders(buf, res.headers());
        appendHeaders(buf, res.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    private static void appendFullCommon(StringBuilder buf, FullHttpMessage msg) {
        buf.append(StringUtil.simpleClassName(msg));
        buf.append("(decodeResult: ");
        buf.append(msg.decoderResult());
        buf.append(", version: ");
        buf.append(msg.protocolVersion());
        buf.append(", content: ");
        buf.append(msg.content());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendInitialLine(StringBuilder buf, HttpRequest req) {
        buf.append(req.method());
        buf.append(' ');
        buf.append(req.uri());
        buf.append(' ');
        buf.append(req.protocolVersion());
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendInitialLine(StringBuilder buf, HttpResponse res) {
        buf.append(res.protocolVersion());
        buf.append(' ');
        buf.append(res.status());
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendHeaders(StringBuilder buf, HttpHeaders headers) {
        for (Map.Entry<String, String> e: headers) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

    private static void removeLastNewLine(StringBuilder buf) {
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    }
    
	public static String unescape(String content) {
		if (content == null) {
			return "";
		}
		content = content.replaceAll("'", "&apos;");
		content = content.replaceAll("\"", "&quot;");
		content = content.replaceAll("\t", "&nbsp;&nbsp;");// 替换跳格
		content = content.replaceAll("<", "&lt;");
		content = content.replaceAll(">", "&gt;");
		return content;
	}
    
    public static boolean isSupportParseString(String contentType) {
    	if (contentType == null) {
    		return false;
    	}
    	for (String supportParsedString : SUPPORT_PARSED_STRING) {
    		if (contentType.toLowerCase().contains(supportParsedString)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static InetAddress parse2InetAddress(HttpRequest request, boolean isHttps) {
    	if (request.headers().get(HttpConstant.Host) == null) {
    		log.error("parse2InetAddress.error, nofound header 'host', request=" + request);
    		return null;
    	}
		// read host and port from http-request
		String[] hostAndPort = request.headers().get(HttpConstant.Host).split(":");
		String host = hostAndPort[0];
		int port = guessPort(isHttps, hostAndPort);
		return new InetAddress(host, port);
    }
	
	private static int guessPort(boolean isHttps, String[] hostAndPort) {
		if (hostAndPort.length == 2) {
			return Integer.parseInt(hostAndPort[1]);
		} else if (isHttps) {
			return HttpConstant.DEFAULT_HTTPS_PORT;
		} else {
			return HttpConstant.DEFAULT_HTTP_PORT;
		}
	}
	
	public static byte[] readBytes(ByteBuf byteBuf) {
		byte[] bytes = new byte[byteBuf.readableBytes()];
		byteBuf.duplicate().readBytes(bytes);
		return bytes;
	}
	
	public static boolean isHttpsRequest(HttpRequest request) {
		return HttpConstant.HTTPS_HANDSHAKE_METHOD.equalsIgnoreCase(request.method().name());
	}

	public static String parse2RelativeURI(String uri) {
		if (uri.startsWith("http://")) {
			return uri.replaceAll("http://.*?/", "/");
		} else if (uri.startsWith("https://")) {
			return uri.replaceAll("https://.*?/", "/");
		} else if (!uri.contains("/")) {
			return "/"; // HTTPS的Connect请求，URI是域名+端口，因此解析出URI为“/”
		}
		return uri;
	}

    private HttpMessageUtil() { }
    
    @Getter
    @AllArgsConstructor
    public static class InetAddress {
    	private String host;
    	private int port;
    	
    	@Override
    	public String toString() {
    		return host + ":" + port;
    	}
    }
}
