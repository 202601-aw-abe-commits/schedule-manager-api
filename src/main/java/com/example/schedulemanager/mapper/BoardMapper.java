package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.BoardPost;
import com.example.schedulemanager.model.BoardJoinRequest;
import com.example.schedulemanager.model.BoardPostInterest;
import com.example.schedulemanager.model.BoardThread;
import com.example.schedulemanager.model.FriendUser;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BoardMapper {

    @Insert("""
            INSERT INTO board_thread (owner_user_id, game_title)
            VALUES (#{ownerUserId}, #{gameTitle})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertThread(BoardThread thread);

    @Select("""
            <script>
            SELECT bt.id, bt.owner_user_id, u.username AS owner_username, u.display_name AS owner_display_name,
                   bt.game_title, bt.created_at, bt.updated_at,
                   COALESCE(COUNT(bp.id), 0) AS post_count,
                   COALESCE(MAX(bp.created_at), bt.created_at) AS latest_post_at
            FROM board_thread bt
            JOIN app_user u ON u.id = bt.owner_user_id
            LEFT JOIN board_post bp ON bp.thread_id = bt.id
            <where>
                <if test="keyword != null and keyword != ''">
                    bt.game_title LIKE CONCAT('%', #{keyword}, '%')
                </if>
            </where>
            GROUP BY bt.id, bt.owner_user_id, u.username, u.display_name, bt.game_title, bt.created_at, bt.updated_at
            ORDER BY latest_post_at DESC, bt.id DESC
            </script>
            """)
    List<BoardThread> findAllThreads(@Param("keyword") String keyword);

    @Select("""
            SELECT id, owner_user_id, game_title, created_at, updated_at
            FROM board_thread
            WHERE id = #{threadId}
            """)
    BoardThread findThreadById(@Param("threadId") Long threadId);

    @Select("""
            SELECT bt.id, bt.owner_user_id, u.username AS owner_username, u.display_name AS owner_display_name,
                   bt.game_title, bt.created_at, bt.updated_at,
                   COALESCE(COUNT(bp.id), 0) AS post_count,
                   COALESCE(MAX(bp.created_at), bt.created_at) AS latest_post_at
            FROM board_thread bt
            JOIN app_user u ON u.id = bt.owner_user_id
            LEFT JOIN board_post bp ON bp.thread_id = bt.id
            WHERE bt.id = #{threadId}
            GROUP BY bt.id, bt.owner_user_id, u.username, u.display_name, bt.game_title, bt.created_at, bt.updated_at
            """)
    BoardThread findThreadViewById(@Param("threadId") Long threadId);

    @Insert("""
            INSERT INTO board_post (thread_id, author_user_id, body, schedule_date, start_time, rank_band, recruitment_limit, discord_invite_url)
            VALUES (#{threadId}, #{authorUserId}, #{body}, #{scheduleDate}, #{startTime}, #{rankBand}, #{recruitmentLimit}, #{discordInviteUrl})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPost(BoardPost post);

    @Select("""
            SELECT bp.id, bp.thread_id, bp.author_user_id, u.username AS author_username, u.display_name AS author_display_name,
                   bp.body, bp.schedule_date, bp.start_time, bp.rank_band, bp.recruitment_limit, bp.discord_invite_url, bp.created_at
            FROM board_post bp
            JOIN app_user u ON u.id = bp.author_user_id
            WHERE bp.thread_id = #{threadId}
            ORDER BY bp.created_at DESC, bp.id DESC
            """)
    List<BoardPost> findPostsByThreadId(@Param("threadId") Long threadId);

    @Select("""
            SELECT bp.id, bp.thread_id, bp.author_user_id, u.username AS author_username, u.display_name AS author_display_name,
                   bp.body, bp.schedule_date, bp.start_time, bp.rank_band, bp.recruitment_limit, bp.discord_invite_url, bp.created_at
            FROM board_post bp
            JOIN app_user u ON u.id = bp.author_user_id
            WHERE bp.id = #{postId}
            """)
    BoardPost findPostById(@Param("postId") Long postId);

    @Insert("""
            INSERT INTO board_post_interest (post_id, requester_user_id, comment)
            VALUES (#{postId}, #{requesterUserId}, #{comment})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPostInterest(BoardPostInterest interest);

    @Select("""
            SELECT bpi.id, bpi.post_id, bpi.requester_user_id, u.username AS requester_username,
                   u.display_name AS requester_display_name, bpi.comment, bpi.created_at
            FROM board_post_interest bpi
            JOIN app_user u ON u.id = bpi.requester_user_id
            WHERE bpi.post_id = #{postId}
            ORDER BY bpi.created_at DESC, bpi.id DESC
            """)
    List<BoardPostInterest> findInterestsByPostId(@Param("postId") Long postId);

    @Update("""
            UPDATE board_thread
            SET updated_at = CURRENT_TIMESTAMP
            WHERE id = #{threadId}
            """)
    int touchThreadUpdatedAt(@Param("threadId") Long threadId);

    @Select("""
            SELECT COUNT(*)
            FROM board_post_participant
            WHERE post_id = #{postId}
            """)
    int countParticipants(@Param("postId") Long postId);

    @Select("""
            SELECT COUNT(*) > 0
            FROM board_post_participant
            WHERE post_id = #{postId}
              AND participant_user_id = #{userId}
            """)
    boolean existsParticipant(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("""
            SELECT u.id, u.username, u.display_name, u.profile_icon_color,
                   CASE WHEN u.profile_image_data IS NULL THEN FALSE ELSE TRUE END AS has_profile_image
            FROM board_post_participant bpp
            JOIN app_user u ON u.id = bpp.participant_user_id
            WHERE bpp.post_id = #{postId}
            ORDER BY bpp.created_at ASC, bpp.id ASC
            """)
    List<FriendUser> findParticipants(@Param("postId") Long postId);

    @Select("""
            SELECT id, post_id, requester_user_id, comment, status, created_at, updated_at
            FROM board_post_join_request
            WHERE post_id = #{postId}
              AND requester_user_id = #{requesterUserId}
            """)
    BoardJoinRequest findJoinRequestByPostAndRequester(
            @Param("postId") Long postId,
            @Param("requesterUserId") Long requesterUserId);

    @Select("""
            SELECT jr.id, jr.post_id, jr.requester_user_id,
                   u.username AS requester_username,
                   u.display_name AS requester_display_name,
                   u.profile_icon_color AS requester_profile_icon_color,
                   CASE WHEN u.profile_image_data IS NULL THEN FALSE ELSE TRUE END AS requester_has_profile_image,
                   jr.comment, jr.status, jr.created_at, jr.updated_at
            FROM board_post_join_request jr
            JOIN app_user u ON u.id = jr.requester_user_id
            WHERE jr.post_id = #{postId}
              AND jr.status = 'PENDING'
            ORDER BY jr.created_at ASC, jr.id ASC
            """)
    List<BoardJoinRequest> findPendingJoinRequestsByPost(@Param("postId") Long postId);

    @Insert("""
            INSERT INTO board_post_join_request (post_id, requester_user_id, comment, status)
            VALUES (#{postId}, #{requesterUserId}, #{comment}, #{status})
            """)
    int insertJoinRequest(
            @Param("postId") Long postId,
            @Param("requesterUserId") Long requesterUserId,
            @Param("comment") String comment,
            @Param("status") String status);

    @Update("""
            UPDATE board_post_join_request
            SET comment = #{comment},
                status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateJoinRequest(
            @Param("id") Long id,
            @Param("comment") String comment,
            @Param("status") String status);

    @Update("""
            UPDATE board_post_join_request
            SET status = #{status},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateJoinRequestStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("""
            INSERT INTO board_post_participant (post_id, participant_user_id)
            VALUES (#{postId}, #{userId})
            """)
    int insertParticipant(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("""
            UPDATE board_post
            SET discord_invite_url = #{discordInviteUrl}
            WHERE id = #{postId}
              AND author_user_id = #{authorUserId}
            """)
    int updateDiscordInviteByAuthor(
            @Param("postId") Long postId,
            @Param("authorUserId") Long authorUserId,
            @Param("discordInviteUrl") String discordInviteUrl);

    @Update("""
            UPDATE board_post
            SET body = #{body},
                schedule_date = #{scheduleDate},
                start_time = #{startTime},
                rank_band = #{rankBand},
                recruitment_limit = #{recruitmentLimit}
            WHERE id = #{postId}
              AND author_user_id = #{authorUserId}
            """)
    int updatePostByAuthor(
            @Param("postId") Long postId,
            @Param("authorUserId") Long authorUserId,
            @Param("body") String body,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("rankBand") String rankBand,
            @Param("recruitmentLimit") Integer recruitmentLimit);

    @Delete("""
            DELETE FROM board_post
            WHERE id = #{postId}
              AND author_user_id = #{authorUserId}
            """)
    int deletePostByAuthor(@Param("postId") Long postId, @Param("authorUserId") Long authorUserId);
}
