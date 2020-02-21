package WeChat.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import WeChat.function.ChatBean;
import WeChat.function.ClientBean;

public class ChatServer {
	private static ServerSocket ss;
	public static HashMap<String, ClientBean> onlines;
	static {
		try {
			ss = new ServerSocket(8500);
			onlines = new HashMap<String, ClientBean>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class CatClientThread extends Thread {
		private Socket client;
		private ChatBean bean;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		public CatClientThread(Socket client) {
			this.client = client;
		}
		@Override
		public void run() {
			try {
				// ��ͣ�Ĵӿͻ��˽�����Ϣ
				while (true) {
					// ��ȡ�ӿͻ��˽��յ���catbean��Ϣ
					ois = new ObjectInputStream(client.getInputStream());
					bean = (ChatBean)ois.readObject();					
					// ����catbean�У�type������һ������
					switch (bean.getType()) {
					// �����߸���
					case 0: { // ����
						// ��¼���߿ͻ����û����Ͷ˿���clientbean��
						ClientBean cbean = new ClientBean();
						cbean.setName(bean.getName());
						cbean.setSocket(client);
						// ���������û�
						onlines.put(bean.getName(), cbean);
						// ������������catbean�������͸��ͻ���
						ChatBean serverBean = new ChatBean();
						serverBean.setType(0);
						serverBean.setInfo(bean.getTimer() + "  "
								+ bean.getName() + "������");
						// ֪ͨ���пͻ���������
						HashSet<String> set = new HashSet<String>();
						// �ͻ��ǳ�
						set.addAll(onlines.keySet());
						serverBean.setClients(set);
						sendAll(serverBean);
						break;
					}
					case -1: { // ����
						// ������������catbean�������͸��ͻ���
						ChatBean serverBean = new ChatBean();
						serverBean.setType(-1);
						try {
							oos = new ObjectOutputStream(
									client.getOutputStream());
							oos.writeObject(serverBean);
							oos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						onlines.remove(bean.getName());
						// ��ʣ�µ������û����������뿪��֪ͨ
						ChatBean serverBean2 = new ChatBean();
						serverBean2.setInfo(bean.getTimer() + "  "
								+ bean.getName() +  " ������");
						serverBean2.setType(0);
						HashSet<String> set = new HashSet<String>();
						set.addAll(onlines.keySet());
						serverBean2.setClients(set);

						sendAll(serverBean2);
						return;
					}
					case 1: { // ����						
//						 ������������catbean�������͸��ͻ���
						ChatBean serverBean = new ChatBean();
						serverBean.setType(1);
						serverBean.setClients(bean.getClients());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());
						// ��ѡ�еĿͻ���������
						sendMessage(serverBean);
						break;
					}					
					default: {
						break;
					}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close();
			}
		}
		// ��ѡ�е��û���������
		private void sendMessage(ChatBean serverBean) {
			Set<String> cbs = onlines.keySet();//�õ����ߵ��û���
			Iterator<String> it = cbs.iterator();			
			HashSet<String> clients = serverBean.getClients();//ѡ�пͻ�
			while (it.hasNext()) {
				// ���߿ͻ�
				String client = it.next();
				// ѡ�еĿͻ����������ߵģ��ͷ�������Ϣ
				if (clients.contains(client)) {
					Socket c = onlines.get(client).getSocket();
					ObjectOutputStream oos;
					try {
						oos = new ObjectOutputStream(c.getOutputStream());
						oos.writeObject(serverBean);
						oos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// �����е��û���������
		public void sendAll(ChatBean serverBean) {
			Collection<ClientBean> clients = onlines.values();
			Iterator<ClientBean> it = clients.iterator();//������
			ObjectOutputStream oos;
			while (it.hasNext()) {
				Socket c = it.next().getSocket();
				try {
					oos = new ObjectOutputStream(c.getOutputStream());
					oos.writeObject(serverBean);
					oos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void close() {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void start() {
		try {
			while (true) {
				Socket client = ss.accept();
				new CatClientThread(client).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new ChatServer().start();
	}

}