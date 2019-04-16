package at.fungus.pi;

import java.math.BigInteger;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.aii.af.lib.mp.module.ModuleHome;
import com.sap.aii.af.lib.mp.module.ModuleLocal;
import com.sap.aii.af.lib.mp.module.ModuleLocalHome;
import com.sap.aii.af.lib.mp.module.ModuleRemote;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.engine.interfaces.messaging.api.exception.PayloadFormatException;

/**
 * Session Bean implementation class AddBytes
 */

@Stateless(name="AddBytesBean")
@Local(value={ModuleLocal.class})
@Remote(value={ModuleRemote.class})
@LocalHome(value=ModuleLocalHome.class)
@RemoteHome(value=ModuleHome.class)
public class AddBytes implements Module{
	private AuditAccess audit;

    /**
     * Default constructor. 
     */
    public AddBytes() {
        // TODO Auto-generated constructor stub
    }

	@Override
	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) throws ModuleException {
		Message msg = (Message)inputModuleData.getPrincipalData();
		MessageKey mk = msg.getMessageKey();
		audit.addAuditLogEntry(mk, AuditLogStatus.SUCCESS, "AddBytes V1.0 started");
		
		// get and validate position
		String position=moduleContext.getContextData("position");
		if (position==null) throw new ModuleException("Please provide module parameter 'position'");
		if (!position.equals("top") && !position.equals("end")) throw new ModuleException("Parameter position must be 'top' or 'end'");
		
		// get bytes
		String bytesString=moduleContext.getContextData("bytes");
		if (bytesString==null) throw new ModuleException("Please provide module parameter 'bytes'");
		String[] bytesElements=bytesString.split(",");
		byte[]   bytes=convertStringToBytes(bytesElements);
		
		// get current payload and content
		Payload payload = msg.getMainPayload();
		byte[] content = payload.getContent();
		byte[] newContent=new byte[content.length+bytes.length];
		
		if (position.equals("top")) {
			System.arraycopy(bytes,   0, newContent, 0,            bytes.length);
			System.arraycopy(content, 0, newContent, bytes.length, content.length);
		} else {
			System.arraycopy(content, 0, newContent, 0,              content.length);
			System.arraycopy(bytes,   0, newContent, content.length, bytes.length);
		}
		
		try {
			payload.setContent(newContent);
			msg.setMainPayload(payload);
		} catch (InvalidParamException e) {
			throw new ModuleException("error during payload.setContent(newContent): "+e.getMessage());
		} catch (PayloadFormatException e) {
			throw new ModuleException("error during payload.setContent(newContent): "+e.getMessage());
		}
		
		return inputModuleData;
	}
	
	private byte[] convertStringToBytes(String[] bytesElements) {
		byte[] result=new byte[bytesElements.length];
		
		for (int i =0;i<bytesElements.length;i++) {
			String oneElement=bytesElements[i];
			result[i]=0;
			
			if (oneElement==null) continue;	// skip null element
			oneElement=oneElement.trim();
			if (oneElement.equals("")) continue;	//skip empty elements
			
			if (oneElement.charAt(0)=='x' || oneElement.charAt(0)=='X') {
				result[i]=new BigInteger(oneElement.substring(1).trim(),16).byteValue();
				continue;				
			}
			
			result[i]=new BigInteger(oneElement).byteValue();
			
		}
		
		return result;
	}

	@PostConstruct
	public void initialiseResources() {
		try {
			audit = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
		} catch (Exception e) {
			throw new RuntimeException("error in initialiseResources():"+e.getMessage());
		}
	}

}
