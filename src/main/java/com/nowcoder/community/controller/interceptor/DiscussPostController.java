package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user=hostHolder.getUser();
        //如果在本次请求中持有用户 在多线程中隔离存放
        if(user==null)
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        return CommunityUtil.getJSONString(0,"发布成功！");
        //报错的情况将来统一处理
    }


    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //从路径中获取变量@PathVariable
        //查询请求
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //帖子
        model.addAttribute("user",userService.findUserById(post.getUserId()) );
        //作者

        //点赞
        long count=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
        model.addAttribute("likeCount",count);
        //点赞状态
        int status;
        if(hostHolder.getUser()==null)
            status=0;
        else
            status=likeService.isLike(hostHolder.getUser().getId(),ENTITY_TYPE_POST,post.getId());
        model.addAttribute("likeStatus",status);

        //评论
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        //分为评论和回复
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论显示列表
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo=new HashMap<>();
                //添加评论
                commentVo.put("comment",comment);
                //添加评论作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //点赞
                count=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",count);
                //点赞状态
                if(hostHolder.getUser()==null)
                    status=0;
                else
                    status=likeService.isLike(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",status);

                //回复列表
                List<Comment> replayList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replayVoList=new ArrayList<>();
                if(replayList!=null){
                     for(Comment reply:replayList){
                         Map<String,Object> replyVo=new HashMap<>();
                         //添加回复
                         replyVo.put("reply",reply);
                         replyVo.put("user",userService.findUserById(reply.getUserId()));
                         //回复的目标
                         User target=reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                         replyVo.put("target",target);

                         //点赞
                         count=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                         replyVo.put("likeCount",count);
                         //点赞状态
                         if(hostHolder.getUser()==null)
                             status=0;
                         else
                             status=likeService.isLike(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                         replyVo.put("likeStatus",status);

                         replayVoList.add(replyVo);
                     }
                }
                //回复装到CommentVO里
                commentVo.put("replys",replayVoList);

                //回复的数量
                commentVo.put("replyCount",commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId()));
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";

    }



}
