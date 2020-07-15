package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
        //重构 事务性
        /*
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember)
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        else
            redisTemplate.opsForSet().add(entityLikeKey,userId);
         */
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey=RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
                //事务中不会查询
                redisOperations.multi();
                if(isMember){
                    redisTemplate.opsForValue().decrement(userLikeKey);
                    redisTemplate.opsForSet().remove(entityLikeKey,userId);
                }
                else {
                    redisTemplate.opsForValue().increment(userLikeKey);
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                }
                redisOperations.exec();
                return null;
            }
        });
    }


    //查询实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //统计某人是否点赞
    public int isLike(int userId,int entityType,int entityId){
        String entityLikeKey= RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        boolean isMember=redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        return isMember?1:0;
    }

    //查询某个用户得到的赞
    public int findUserLikeCount(int userId){
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);
        Integer count=(Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }
}
