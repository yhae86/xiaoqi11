package com.m.runtime;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

class Log extends Thread{
	
	public static PriorityBlockingQueue<String> logMessages = new PriorityBlockingQueue<String>();
	public static StringBuilder  sb;
	public static List<String> 	mode = new ArrayList<String>();;
	Log(){

		 sb = new  StringBuilder();
		mode.add("can_join_groups=null, ");
		mode.add("can_read_all_group_messages=null, ");
		mode.add("supports_inline_queries=null");
		mode.add("first_name='null', ");
		mode.add("last_name='null', ");
		mode.add("username='null', ");
		mode.add("photo=null, ");
		mode.add("description='null', ");
		mode.add("invite_link='null', ");
		mode.add("pinned_message=null, ");
		mode.add("permissions=null, ");
		mode.add("slow_mode_delay=null, ");
		mode.add("sticker_set_name='null', ");
		mode.add("can_set_sticker_set=null");
		mode.add("forward_from=null, ");
		mode.add("forward_from_chat=null, ");
		mode.add("forward_from_message_id=null, ");
		mode.add("forward_signature='null', ");
		mode.add("forward_sender_name='null', ");
		mode.add("forward_date=null, ");
		mode.add("reply_to_message=null, ");
		mode.add("via_bot=null, ");
		mode.add("edit_date=null, ");
		mode.add("media_group_id='null', ");
		mode.add("author_signature='null', ");
		mode.add("text='null', ");
		mode.add("caption_entities=null, ");
		mode.add("entities=null, ");
		mode.add("audio=null, ");
		mode.add("document=null, ");
		mode.add("animation=null, ");
		mode.add("game=null, ");
		mode.add("photo=null, ");
		mode.add("sticker=null, ");
		mode.add("video=null, ");
		mode.add("voice=null, ");
		mode.add("video_note=null, ");
		mode.add("caption='null', ");
		mode.add("contact=null, ");
		mode.add("location=null, ");
		mode.add("venue=null, ");
		mode.add("poll=null, ");
		mode.add("dice=null, ");
		mode.add("new_chat_members=null, ");
		mode.add("left_chat_member=null, ");
		mode.add("last_name='null', ");
		mode.add("language_code='null', ");
		mode.add("can_join_groups=null, ");
		mode.add("can_read_all_group_messages=null, ");
		mode.add("supports_inline_queries=null");
		mode.add("new_chat_title='null', ");
		mode.add("new_chat_photo=null, ");
		mode.add("delete_chat_photo=null, ");
		mode.add("group_chat_created=null, ");
		mode.add("supergroup_chat_created=null, ");
		mode.add("channel_chat_created=null, ");
		mode.add("migrate_to_chat_id=null, ");
		mode.add("migrate_from_chat_id=null, ");
		mode.add("pinned_message=null, ");
		mode.add("invoice=null, successful_payment=null, ");
		mode.add("connected_website='null', ");
		mode.add("passport_data=null, ");
		mode.add("edited_message=null, ");
		mode.add("reply_markup=null");
		mode.add("channel_post=null, ");
		mode.add("edited_channel_post=null, ");
		mode.add("inline_query=null, ");
		mode.add("chosen_inline_result=null, ");
		mode.add("callback_query=null, ");
		mode.add("shipping_query=null, ");
		mode.add("pre_checkout_query=null, ");
		mode.add("poll=null, ");
		mode.add("poll_answer=null");
		mode.add("language='null'");
		mode.add("all_members_are_administrators=null, ");
		mode.add("title='null', ");
		mode.add("user=null, ");
		mode.add("url='null', ");
		mode.add("type=bot_command, ");
		mode.add("proximity_alert_triggered=null, ");
		mode.add("location=null");
		mode.add("linked_chat_id=null, 	");
		mode.add("language_code='zh-hans', ");
		mode.add("sender_chat=null, ");
	}
	 public void run() {
//		 while(true) {
//			 if(Main.adminChatID!=0) {
//				 String up = logMessages.poll();
//				 if(null!=up) {
//					 Main.fasong(Main.adminChatID,delete(up).toString());
//				 }
//			 }
//			 try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		 }
		 
	 }
	 static void put(String up) {
		 if(logMessages.size()<10)
			 logMessages.put(up);
	 }
	 static StringBuilder delete(String up) {
		 sb.delete(0, sb.length());
		 sb.append(up);
		 for(String str:mode)if(sb.indexOf(str)!=-1)sb.delete(sb.indexOf(str), sb.indexOf(str)+str.length());
		return sb;
		
	 }
}
