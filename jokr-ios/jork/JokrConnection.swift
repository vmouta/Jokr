/**
* @name             JokrConnection.swift
* @partof           zucred AG
* @description
* @author	 		Vasco Mouta
* @created			28/12/15
*
* Copyright (c) 2015 zucred AG All rights reserved.
* This material, including documentation and any related
* computer programs, is protected by copyright controlled by
* zucred AG. All rights are reserved. Copying,
* including reproducing, storing, adapting or translating, any
* or all of this material requires the prior written consent of
* zucred AG. This material also contains confidential
* information which may not be disclosed to others without the
* prior written consent of zucred AG.
*/

import Foundation
import zucredApple
import Alamofire
import AlamofireObjectMapper

open class JokrConnection: AppConnection {
    
    open override class var sharedInstance: JokrConnection {
        return super.sharedInstance as! JokrConnection
    }
    
    /// Router URL Generator
    public enum Router: URLRequestConvertible {
        static let logger = AppLogger.logger("Router")
        
        static let hostString = "jokr.eu-gb.mybluemix.net"
        static let baseURLString = "http://" + hostString
        
        case createUser(user: User)
        case recommendedJokes(userID: String, sessionID: String)
        case userJokes(userID: String)
        case deleteUserJokes(jokeID: String)
        case likeJoke(userID: String, jokeID: String)
        case dislikeJoke(userID: String, jokeID: String)
        case addFavorite(userID: String, jokeID: String)
        case removeFavorite(userID: String, jokeID: String)
        case allFavoritesJokes(userID: String)
        case blockUser(userID: String, blockUserID: String)
        case unblockUser(userID: String, blockUserID: String)
        case allblockedUsers(userID: String)
        case followUser(userID: String, followUserID: String)
        case unfollowUser(userID: String, unfollowUserID: String)
        case allFollowingUsers(userID: String)
        case updateJoke(joke: Joke)
        case fetchUserPicture(userID: String)
        case fetchJoke(jokeID: String)
        case uploadJoke()
        case addUserImage()
        
        public var URL: String {
            return URLRequest.URLString
        }
        
        // MARK: URLRequestConvertible
        
        public var URLRequest: NSMutableURLRequest {
            let result: (method: String, path: String, parameters: [String: AnyObject], encoding: Alamofire.ParameterEncoding ) = {
                switch self {
                // User
                case .CreateUser(let user):
                    return ("POST", "/api/user", user.serverDictionary(), .JSON)
                case .AddFavorite(let userID, let jokeID):
                    return ("POST", "/api/user/addFavorite", ["userId": userID, "jokeId": jokeID], .URLEncodedInURL)
                case .RemoveFavorite(let userID, let jokeID):
                    return ("POST", "/api/user/removeFavorite", ["userId": userID, "jokeId": jokeID], .URLEncodedInURL)
                case .AllFavoritesJokes(let userID):
                    return ("GET", "/api/user/getAllFavoritesOfUser", ["userId": userID], .URL)
                case .BlockUser(let userID, let blockUserID):
                    return ("POST", "/api/user/blockUser", ["userId": userID, "userToBlockId": blockUserID], .URLEncodedInURL)
                case .UnblockUser(let userID, let blockUserID):
                    return ("POST", "/api/user/unblockUser", ["userId": userID, "userToBlockId": blockUserID], .URLEncodedInURL)
                case .AllblockedUsers(let userID):
                    return ("GET", "/api/user/getBlockedUsers", ["userId": userID], .URL)
                case .FollowUser(let userID, let followUserID):
                    return ("POST", "/api/user/follow", ["userId": userID, "followUserId": followUserID], .URLEncodedInURL)
                case .UnfollowUser(let userID, let unfollowUserID):
                    return ("POST", "/api/user/unfollow", ["userId": userID, "followUserId": unfollowUserID], .URLEncodedInURL)
                case .AllFollowingUsers(let userID):
                    return ("GET", "/api/user/getFollowers", ["userId": userID], .URL)
                // Fetch
                case .RecommendedJokes(let userID, let sessionID):
                    return ("GET", "/api/fetch", ["userId": userID, "demo": "false", "session": sessionID], .URL)
                // Joke
                case .UserJokes(let userID):
                    return ("GET", "/api/joke/getAllJokesOfUser", ["userId": userID], .URL)
                case .DeleteUserJokes(let jokeID):
                    return ("POST", "/api/joke/delete", ["jokeId": jokeID], .URLEncodedInURL)
                case .LikeJoke(let userID, let jokeID):
                    return ("POST", "/api/joke/like", ["userId": userID, "jokeId": jokeID], .URLEncodedInURL)
                case .DislikeJoke(let userID, let jokeID):
                    return ("POST", "/api/joke/dislike", ["userId": userID, "jokeId": jokeID], .URLEncodedInURL)
                case .UpdateJoke(let joke):
                    return ("POST", "/api/joke/update", joke.toDictionary(), .JSON)
                
                case .FetchUserPicture(let userID):
                    return ("GET", "/fetchUserPic", ["userId": userID], .URL)
                case .FetchJoke(let jokeID):
                    return ("GET", "/fetchJoke", ["jokeId":jokeID], .URL)
                case .UploadJoke():
                    return ("POST", "/receiver", [:], .URL)
                case .AddUserImage():
                    return ("POST", "/addUserImage", [:], .URL)
                }
            }()
            
            let URL = Foundation.URL(string: Router.baseURLString)!
            let URLRequest = NSMutableURLRequest(URL: URL.URLByAppendingPathComponent(result.path))
            URLRequest.HTTPMethod = result.method
            let request = result.encoding.encode(URLRequest, parameters:result.parameters).0
            Router.logger.debug(request.URL)
            Router.logger.verbose(result.2)
            return request
        }
    }
    
    fileprivate func executeRequest(_ request: URLRequestConvertible, resultHandler:(Result<Bool, NSError>) -> Void) -> Request {
        return Alamofire.request(request)
            .validate(statusCode: 200..<300)
            .response { request, response, data, error in
                guard error == nil else {
                    self.logger.error("\(request)\n\(response)\n\(error)")
                    resultHandler(.Failure(error!))
                    return
                }
                self.logger.verbose("Success executeRequest apparently:\(response)")
                resultHandler(.Success(true))
        }
    }
    
    /**
     - parameter login:
     - parameter resultHandler: completion handler
     */
    open func createUser(_ login: Login, resultHandler:(Result<Login, NSError>) -> Void) {
        guard let user = login.getUser() else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error(error)
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.CreateUser(user: user))
            .responseObject{ (response: Response<User, NSError>) in
                self.logger.verbose(response.request)
                self.logger.verbose(response.response)
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    let login = Login(user: value, passToken: "")
                    resultHandler(.Success(login))
                case .Failure(let error):
                    self.logger.error("createUser:\n\(error)")
                    resultHandler(.Failure(error))
                }
            }
    }
    
    /**
     - parameter user:          
     - parameter resultHandler: completion handler
     */
    open func fetchRecommendedJokes(_ user: User, sessionID:String, resultHandler:(Result<[Joke], NSError>) -> Void) {
        guard let userID = user.serverID else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error("fetchRecommendedJokes:\n\(error)")
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.RecommendedJokes(userID: userID, sessionID: sessionID))
            .responseArray{ (response: Response<[Joke], NSError>) in
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    resultHandler(.Success(value))
                case .Failure(let error):
                    self.logger.error("fetchRecommendedJokes:\n\(error)")
                    resultHandler(.Failure(error))
                }
            }
    }
    
    /**
     - parameter user:
     - parameter resultHandler: completion handler
     */
    open func fetchUserJokes(_ user: User, resultHandler:(Result<[Joke], NSError>) -> Void) {
        guard let userID = user.serverID else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error("fetchUserJokes:\n\(error)")
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.UserJokes(userID: userID))
            .responseArray{ (response: Response<[Joke], NSError>) in
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    resultHandler(.Success(value))
                case .Failure(let error):
                    self.logger.error("fetchUserJokes:\n\(error)")
                    resultHandler(.Failure(error))
                }
            }
    }
    
    open func uploadJoke(_ joke: Joke, resultHandler:(Result<Bool, NSError>) -> Void ) {
        
        if let filePath = joke.localUrl {
            if let data = try? Data(contentsOf: URL(fileURLWithPath: filePath)) {
                Alamofire.upload(Router.UploadJoke(), multipartFormData: { multipartFormData in
                        let url = NSURL(string:filePath)
                        let lastComponent = url?.lastPathComponent ?? "sound.caf"
                        multipartFormData.appendBodyPart(data: data, name: "file", fileName: lastComponent, mimeType: "audio/mpeg3")
                        multipartFormData.appendBodyPart(data: joke.modelID.dataUsingEncoding(NSUTF8StringEncoding)!, name: "_id")
                        multipartFormData.appendBodyPart(data: joke.userId.dataUsingEncoding(NSUTF8StringEncoding)!, name: "userId")
                        multipartFormData.appendBodyPart(data: joke.title.dataUsingEncoding(NSUTF8StringEncoding)!, name: "title")
                        multipartFormData.appendBodyPart(data: joke.language.dataUsingEncoding(NSUTF8StringEncoding)!, name: "language")
                        multipartFormData.appendBodyPart(data: joke.tags.dataUsingEncoding(NSUTF8StringEncoding)!, name: "tags")
                        multipartFormData.appendBodyPart(data: String(joke.framingBegin).dataUsingEncoding(NSUTF8StringEncoding)!, name: "framingBegin")
                        multipartFormData.appendBodyPart(data: String(joke.framingEnd).dataUsingEncoding(NSUTF8StringEncoding)!, name: "framingEnd")
                        multipartFormData.appendBodyPart(data: String(joke.creationTimestamp).dataUsingEncoding(NSUTF8StringEncoding)!, name: "creationTimestamp")
                    }, encodingCompletion: { encodingResult in
                        print(encodingResult)
                        switch encodingResult {
                        case .Success(let upload, _, _):
                            self.logger.verbose("Success encoding:\(upload)")
                            upload.response { request, response, data, error in
                                guard error == nil else {
                                    self.logger.error("uploadJoke:\n\(error)")
                                    let error = NSError(domain: error!.domain, code: error!.code, userInfo:[NSLocalizedDescriptionKey:error!.description])
                                    resultHandler(.Failure(error))
                                    return
                                }
                                self.logger.verbose("Success upload apparently:\(response)")
                                resultHandler(.Success(true))
                            }
                        case .Failure(let encodingError):
                            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:[NSLocalizedDescriptionKey:"Error: \(encodingError)"])
                            self.logger.error("uploadJoke - encodingCompletion:\n\(error)")
                            resultHandler(.Failure(error))
                        }
                })
            } else {
                logger.error("Impossible to load joke data from file:\(filePath)")
                let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
                resultHandler(.Failure(error))
            }
        } else {
            logger.error("Incomplete Joke:\(joke)")
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            resultHandler(.Failure(error))
        }
    }
    
    /**
     Update a joke meta data
     NOTE: the joke it self is not updated
     
     - parameter joke:          the joke metadata to be updated
     - parameter resultHandler: completion handler
     */
    open func updateJoke(_ joke: Joke, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.UpdateJoke(joke: joke), resultHandler:resultHandler)
    }
    
    open func uploadProfilePicture(_ loginUser: Login, imageData: Data, resultHandler:(Result<Bool, NSError>) -> Void) {
        Alamofire.upload(Router.AddUserImage(), multipartFormData: { multipartFormData in
                multipartFormData.appendBodyPart(data: imageData, name: "file", fileName: "file.png", mimeType: "image/png")
                if let userId = loginUser.getUser()?.serverID {
                    multipartFormData.appendBodyPart(data: userId.dataUsingEncoding(NSUTF8StringEncoding)!, name: "userId")
                }
            }, encodingCompletion: { encodingResult in
                switch encodingResult {
                case .Success(let upload, _, _):
                    self.logger.verbose("Success encoding:\(upload)")
                    upload.response {request, response, data, error in
                        guard error == nil else {
                            self.logger.error("uploadProfilePicture:\n\(error)")
                            resultHandler(.Failure(error!))
                            return
                        }
                        self.logger.verbose("Success upload apparently:\(response)")
                        resultHandler(.Success(true))
                    }
                case .Failure(let encodingError):
                    let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:[NSLocalizedDescriptionKey:"Error: \(encodingError)"])
                    self.logger.error("uploadProfilePicture - encodingCompletion:\n\(error)")
                    resultHandler(.Failure(error))
                }
        })
    }
    
    open func downloadProfilePicure(_ userID: String, resultHandler:(Result<UIImage, NSError>) -> Void ) {
        Alamofire.request(Router.FetchUserPicture(userID: userID))
            .response{ (request, response, data, error) in
                guard error == nil else {
                    self.logger.error("downloadProfilePicure:\n\(error)")
                    resultHandler(.Failure(error!))
                    return
                }
                if let image = UIImage(data: data!) {
                    self.logger.verbose("Image \(request?.URL) download with success!")
                    resultHandler(.Success(image))
                } else {
                    let error = NSError(domain: JokrConnection.name(), code: -2, userInfo:nil)
                    self.logger.error("downloadProfilePicure:\n\(error)")
                    resultHandler(.Failure(error))
                }
            }
    }
    
    open func likeJoke(_ userId: String, jokeId: String, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.LikeJoke(userID:userId, jokeID: jokeId), resultHandler:resultHandler)
    }
    
    open func dislikeJoke(_ userId: String, jokeId: String, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.DislikeJoke(userID:userId, jokeID: jokeId), resultHandler:resultHandler)
    }
    
    open func followUser(_ userId: String, followUserID: String, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.FollowUser(userID:userId, followUserID: followUserID), resultHandler:resultHandler)
    }
    
    open func unfollowUser(_ userId: String, unfollowUserID: String, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.UnfollowUser(userID:userId, unfollowUserID: unfollowUserID), resultHandler:resultHandler)
    }
    
    open func getAllFollowUsers(_ user: User, resultHandler:(Result<[User], NSError>) -> Void) {
        guard let userID = user.serverID else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error(error)
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.AllFollowingUsers(userID: userID))
            .responseArray{ (response: Response<[User], NSError>) in
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    resultHandler(.Success(value))
                case .Failure(let error):
                    self.logger.error("getAllFollowUsers:\n\(error)")
                    resultHandler(.Failure(error))
                }
        }
    }
    
    /**
     Get all the blocked users from the user
     
     - parameter user:          user to fetch the blocked ones
     - parameter resultHandler: completion handler
     */
    open func getAllBlockedUsers(_ user: User, resultHandler:(Result<[User], NSError>) -> Void) {
        guard let userID = user.serverID else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error(error)
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.AllblockedUsers(userID: userID))
            .responseArray{ (response: Response<[User], NSError>) in
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    resultHandler(.Success(value))
                case .Failure(let error):
                    self.logger.error("getAllBlockedUsers:\n\(error)")
                    resultHandler(.Failure(error))
                }
        }
    }
    
    /**
     Delete an user joke
     
     - parameter userId:        user of the joke
     - parameter resultHandler: completion handler
     */
    open func deleteJoke(_ jokeId: String, resultHandler:(Result<Bool, NSError>) -> Void) {
        executeRequest(Router.DeleteUserJokes(jokeID: jokeId), resultHandler:resultHandler)
    }
    
    /**
     Add a joke to the user favorite jokes
     
     - parameter userId:        user of the favorites
     - parameter jokeId:        joke to add to favorites
     - parameter resultHandler: completion handler
     */
    open func addFavorite(_ userId: String, jokeId: String, resultHandler:(Result<Bool, NSError>) -> Void ) {
        executeRequest(Router.AddFavorite(userID: userId, jokeID: jokeId), resultHandler:resultHandler)
    }
    
    /**
     Remove a joke from the user favorite jokes
     
     - parameter userId:        user of the favorites
     - parameter jokeId:        joke to remove from favorites
     - parameter resultHandler: completion handler
     */
    open func removeFavorite(_ userId: String, jokeId: String, resultHandler:(Result<Bool, NSError>) -> Void) {
        executeRequest(Router.RemoveFavorite(userID: userId, jokeID: jokeId), resultHandler:resultHandler)
    }
    
    /**
     Get all user favorites
     
     - parameter user:          user of the favorites
     - parameter resultHandler: completion handler
     */
    open func getAllFavoritesOfUser(_ user: User, resultHandler:(Result<[Joke], NSError>) -> Void) {
        guard let userServerID = user.serverID else {
            let error = NSError(domain: JokrConnection.name(), code: -1, userInfo:nil)
            self.logger.error("getAllFavoritesOfUser:\n\(error)")
            resultHandler(.Failure(error))
            return
        }
        Alamofire.request(Router.AllFavoritesJokes(userID: userServerID))
            .responseArray{ (response: Response<[Joke], NSError>) in
                switch response.result {
                case .Success(let value):
                    self.logger.verbose(value)
                    resultHandler(.Success(value))
                case .Failure(let error):
                    self.logger.error("getAllFavoritesOfUser:\n\(error)")
                    resultHandler(.Failure(error))
                }
        }
    }
    
    /**
     Block jokes from a specific user (userToBolckId) for the user userId
     
     - parameter userId:        user that wants to block
     - parameter userToBlockId: user to be blocked
     - parameter resultHandler: completion handler
     */
    open func blockUser(_ userId: String, userToBlockId:String, resultHandler:(Result<Bool, NSError>) -> Void) {
        executeRequest(Router.BlockUser(userID:userId, blockUserID: userToBlockId), resultHandler:resultHandler)
    }
    
    /**
     Block jokes from a specific user (userToBolckId) for the user userId
     
     - parameter userId:        user that wants to block
     - parameter userToBlockId: user to be blocked
     - parameter resultHandler: completion handler
     */
    open func unblockUser(_ userId: String, userToUnblockId:String, resultHandler:(Result<Bool, NSError>) -> Void) {
        executeRequest(Router.UnblockUser(userID:userId, blockUserID: userToUnblockId), resultHandler:resultHandler)
    }
}

// MARK: Reachability
extension JokrConnection {

    override public func reachabilityHost() -> String {
        return Router.hostString
    }
    
    override public func reachabilityChanged(_ note: Notification) {
        super.reachabilityChanged(note)
        if !(note.object as! Reachability).isReachable() {

        } else {
            
        }
    }
}
