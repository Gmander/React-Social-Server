package com.revature.posts;

import com.revature.comments.Comment;
import com.revature.comments.dtos.AuthorDto;
import com.revature.comments.dtos.CommentRequest;
import com.revature.follow.FollowRepository;
import com.revature.groups.Group;
import com.revature.groups.GroupRepository;
import com.revature.groups.dtos.GroupResponse;
import com.revature.posts.dtos.NewPostRequest;
import com.revature.posts.dtos.PostResponse;
import com.revature.exceptions.UserNotFoundException;
import com.revature.posts.postmeta.PostMeta;
import com.revature.users.User;
import com.revature.comments.CommentRepository;
import com.revature.posts.postmeta.PostMetaRepository;
import com.revature.users.UserRepository;
import com.revature.users.profiles.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
	private final FollowRepository followRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ProfileRepository profileRepository;
	private final PostMetaRepository postMetaRepository;
	private final UserRepository userRepository;

	// constructor
	@Autowired
	public PostService(PostRepository postRepository, CommentRepository commentRepository,
			ProfileRepository profileRepository, PostMetaRepository postMetaRepository, FollowRepository followRepository, UserRepository userRepository) {
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.profileRepository = profileRepository;
		this.postMetaRepository = postMetaRepository;
		this.followRepository = followRepository;
		this.userRepository = userRepository;
	}

	/*  No parameters
		Returns all Post objects in database
	 */
	public List<PostResponse> getPosts() {
		List<Post> rawRepository = postRepository.findAll();
		List<PostResponse> refinedRepo = new LinkedList<>();

		for (int i = 0; i < rawRepository.size(); i++) {
			// Record the relevant data from the posts.
			Post rawPost = rawRepository.get(i);
			PostResponse refinedPost = new PostResponse(rawPost);

			// Get the post's comments
			List<Comment> rawComments = rawPost.getComments();
			List<CommentRequest> refinedComments = new LinkedList<>();
			for (int j = 0; j < rawComments.size(); j++){
				// Record the relevant data for a comment.
				Comment rawComment = rawComments.get(j);
				CommentRequest refinedComment = new CommentRequest();

				// Get the simple values
				refinedComment.setCommentId(rawComment.getId().toString());
				refinedComment.setCommentText(rawComment.getCommentText());
				refinedComment.setDate(rawComment.getDate());

				// Create the author object we need
				AuthorDto refinedAuthor = new AuthorDto(rawComment.getAuthor(), profileRepository);
				refinedComment.setAuthor(refinedAuthor);

				// Add the result to the list
				refinedComments.add(refinedComment);

			}
			refinedPost.setComments(refinedComments);

			refinedRepo.add(refinedPost);
		}

		return refinedRepo;
	}



	/**
	 * @param userId
	 * @return all post objects attached to a userId
	 */
	public List<PostResponse> getPostsOfUserId(String userId) {
		List<PostResponse> allPosts = getPosts();
		List<PostResponse> filteredPosts = new ArrayList<>();
		for(int i = 0; i < allPosts.size(); i++){
			if(allPosts.get(i).getAuthorID().equals(userId)){
				filteredPosts.add(allPosts.get(i));
			}
		}
		if(filteredPosts.size() == 0) return null; //TODO: make exception to throw here
		else return filteredPosts;
	}

	public List<PostResponse> getPersonalPosts(String userId){
		List<PostResponse> personalPosts = new ArrayList<>();
		List<PostResponse> followingPosts = getPostsOfFollowing(userId);
		//retrieve user posts
		List<PostResponse> userPosts = getPostsOfUserId(userId);
		//retrieve group posts
		User user = userRepository.getById(userId);
		List<Group> groups = user.getGroups();
		for(Group g : groups){
			g.getId();
			//find posts by groupId
			// public List<Post> findPostsByGroupId(Group group);
		}

		//combine lists
		//int size = followingPosts.size() + userPosts.size(); //+ groupPosts.size();
		for(PostResponse p : followingPosts) personalPosts.add(p);
		for(PostResponse p : userPosts) personalPosts.add(p);
		//for(PostResponse p : groupPosts) personalPosts.add(p);

		//TODO: sort combined list by date
//		personalPosts.sort(e -> e.getDate().getNano());

		//sorts post by date. .reversed() should sort newest to oldest
		personalPosts.stream()
				.sorted(Comparator.comparing(PostResponse::getDate).reversed())
				.collect(Collectors.toList());


		return personalPosts;
	}

//	public List<PostResponse> sortPosts(ListPost


	/**
	 * no parameters
	 * @returns all post objects attached to the userIds that the logged-in user is following
	 */
	public List<PostResponse> getPostsOfFollowing(String userId) {
		List<PostResponse> followingPosts = new ArrayList<>();

		//get all people following a given user and pull out user Ids.
		User followingUser = followRepository.findById(userId).get();
		List<User> following = followingUser.getFollowing();

		for(int i = 0; i < following.size(); i++){
			List<PostResponse> filteredPosts = getPostsOfUserId(following.get(i).getId());
			followingPosts.addAll(filteredPosts);
		}
		//System.out.println("right before followingPost returns");
		return followingPosts;
	}

	/*  Parameters: Post object, User object
		Adds a new Post to the database, registered to specific User
		Returns the Post added to the database
	 */

	public Post addNewPost(NewPostRequest post, User user) throws UserNotFoundException
    {
		// Create a post meta and a post
		PostMeta newPostMeta = new PostMeta();
		Post newPost = new Post();

		// Set the author
        newPostMeta.setAuthor(user);

		//post.setId(UUID.randomUUID());


		// Set the time of the post
        newPostMeta.setDate(LocalDateTime.now(ZoneOffset.UTC));

		// Set the content type
		newPostMeta.setContentType(post.getContentType());

		// Set the link
		if (post.getContentLink() != null) {
			newPost.setContentLink(post.getContentLink());
		}
		else {
			newPost.setContentLink(null);
		}

		// Set the content
		newPost.setPostText(post.getPostText());

		// Save the meta to the repository
		postMetaRepository.save(newPostMeta);
		newPost.setPostMeta(newPostMeta);

		// Save the new post and return the status
        return postRepository.save(newPost);
    }



	/*  Parameter:  User UID (from Firebase)
		Returns a list of all posts registered to the User
	 */

    /*
	public List<Post> getUserPosts(String authorUID) {
		List<Post> ret = new ArrayList<Post>();
		for (Post p : postRepository.findAll()) {
			if (p.getAuthor().getUid().equals(authorUID)) {
				ret.add(p);
			}
		}
		return ret;
	}

     */

}
