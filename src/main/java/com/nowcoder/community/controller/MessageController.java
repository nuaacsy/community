package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    //处理私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public  String getLetterList(Model model, Page page){

        //获取user
        User user=hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList=messageService.findConversations(
                user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> conversations =new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                Map<String ,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId=user.getId()==message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        //整个用户所有未读消息
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //系统通知
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";

    }


    //查看详细的私信
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    private String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList=messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String ,Object>> letters=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                Map<String ,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }

        }
        model.addAttribute("letters",letters);
        //查询私信的目标
        model.addAttribute("target", getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty())
            messageService.readMessage(ids);
        return "/site/letter-detail";
    }

    //实现把消息未读提取出来
    private List<Integer> getLetterIds(List<Message> letterlist){
        List<Integer> ids=new ArrayList<>();

        if(letterlist!=null){
            for(Message message:letterlist){
                if(message.getToId()==hostHolder.getUser().getId()&&message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId){
        String[] ids=conversationId.split("_");
        int d0=Integer.parseInt(ids[0]);
        int d1=Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId()==d0){
            return userService.findUserById(d1);
        }
        else
            return userService.findUserById(d0);
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target=userService.findUserByName(toName);
        if(target==null)
            return CommunityUtil.getJSONString(1,"目标用户不存在");

        Message message=new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId())
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        else
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        message.setStatus(0);
        message.setCreateTime(new Date());
        message.setContent(content);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user= hostHolder.getUser();
        //评论
        Message message=messageService.findLatestNotice(user.getId(),"comment");
        Map<String,Object> messageVO=new HashMap<>();
        if(message!=null){
            messageVO.put("message",message);
            String content=message.getContent();
            //无转义字符
            content= HtmlUtils.htmlUnescape(content);
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            int noticeCount =messageService.findNoticeCount(user.getId(),"comment");
            int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),"comment");
            messageVO.put("count",noticeCount);
            messageVO.put("unread",noticeUnreadCount);
        }else {
            messageVO.put("message",null);
        }
        model.addAttribute("commentNotice",messageVO);

        //赞
        message=messageService.findLatestNotice(user.getId(),"like");
        messageVO=new HashMap<>();
        if(message!=null){
            messageVO.put("message",message);
            String content=message.getContent();
            //无转义字符
            content= HtmlUtils.htmlUnescape(content);
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            int noticeCount =messageService.findNoticeCount(user.getId(),"like");
            int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),"like");
            messageVO.put("count",noticeCount);
            messageVO.put("unread",noticeUnreadCount);
        }else {
            messageVO.put("message",null);
        }
        model.addAttribute("likeNotice",messageVO);

        //关注
        message=messageService.findLatestNotice(user.getId(),"follow");
        messageVO=new HashMap<>();
        if(message!=null){
            messageVO.put("message",message);
            String content=message.getContent();
            //无转义字符
            content= HtmlUtils.htmlUnescape(content);
            Map<String,Object> data=JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
        }else {
            messageVO.put("message",null);
        }

        int noticeCount =messageService.findNoticeCount(user.getId(),"follow");
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),"follow");
        System.out.println("===========follow我的人数"+noticeCount);
        messageVO.put("count",noticeCount);
        messageVO.put("unread",noticeUnreadCount);
        model.addAttribute("followNotice",messageVO);

        //未读消息数量
        //私信
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //系统通知
        noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

}
