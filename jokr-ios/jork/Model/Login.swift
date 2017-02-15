/**
 * @name             Login.swift
 * @partof           Jokr
 * @description
 * @author	 		Vasco Mouta
 * @created			20/11/15
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
import ObjectMapper

open class Login: AppLogin {
    
    open var recommendedJokes = [Joke]()
    open var favoriteJokes = [Joke]()
    open var myJokes = [Joke]()
    open var myServerJokes = [Joke]()
    
    open var followingUsers = [User] ()
    open var blockedUsers = [User] ()
    
    open var userJokes:[Joke] {
        var jokes:[Joke] = []
        jokes.append(contentsOf: myJokes)
        jokes.append(contentsOf: myServerJokes)
        return jokes
    }
    
    public override init(user: AppUser, passToken: String) {
        super.init(user: user, passToken:passToken)
    }
    
    public init(deviceID: String) {
        super.init(user: User(deviceID:deviceID), passToken:"")
    }
    
    override open func getUser() -> User? {
        return super.getUser() as? User
    }
    
    required public init() {
        super.init()
    }
    
    open func hasFavorite(_ id: String) -> Bool {
        for fav in favoriteJokes {
            if fav.modelID == id {
                return true
            }
        }
        return false
    }
    
    open func hasBlockedUser(_ userServerId: String) -> Bool {
        for user in blockedUsers {
            if let uId = user.serverID, uId == userServerId {
                return true
            }
        }
        return false
    }
    
    open func isFollowingUser(_ userServerId: String) -> Bool {
        for user in followingUsers {
            if let uId = user.serverID, uId == userServerId {
                return true
            }
        }
        return false
    }
    
    open func setFavorite(_ joke: Joke, isFavorite: Bool) {
        if isFavorite {
            if !hasFavorite(joke.modelID) {
                favoriteJokes.append(joke)
            }
        } else {
            for (index, fav) in favoriteJokes.enumerated() {
                if fav.modelID == joke.modelID {
                    favoriteJokes.remove(at: index)
                }
            }
        }
    }
    
    func resizeImage(_ image: UIImage, newWidth: CGFloat) -> UIImage {
        
        let scale = newWidth / image.size.width
        let newHeight = image.size.height * scale
        UIGraphicsBeginImageContext(CGSizeMake(newWidth, newHeight))
        image.drawInRect(CGRectMake(0, 0, newWidth, newHeight))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
    
    open func setLoginUserImage(_ image: UIImage, complitionHandler: (_ success:Bool)-> Void) {
        DispatchQueue.global(priority: DispatchQueue.GlobalQueuePriority.default).async(execute: {
            var success = false
            let thumbnail = self.resizeImage(image, newWidth: 200)
            if let imageData = UIImagePNGRepresentation(thumbnail) {
                if let path = (AppProperties.sharedInstance.documentsDirectory as NSString?)?.stringByAppendingPathComponent(User.UserImageName) {
                    success = imageData.writeToFile(path, atomically: true)
                    if !success {
                        AppLogger.error("Error writting user logo image")
                    } else {
                        JokrConnection.sharedInstance.uploadProfilePicture(self, imageData: imageData) {_ in}
                    }
                } else {
                    AppLogger.error("Error no path to writting user logo image")
                }
            } else {
                // handle error
                AppLogger.error("Error converting image to png data")
            }
            // do some task
            DispatchQueue.main.async(execute: {
                //
                
                // update some UI
                complitionHandler(success: success)
            })
        })
    }
    
    open func getLoginUserImage() -> UIImage? {
        guard let path = (AppProperties.sharedInstance.documentsDirectory as NSString?)?.stringByAppendingPathComponent(User.UserImageName) else {
            return UIImage(named: "ImageUnknown")
        }
        
        guard let image = UIImage(contentsOfFile:path) else {
            return UIImage(named: "ImageUnknown")
        }
        return image
    }
    
    // MARK: Mappable
    
    required public init?(_ map: Map) {
        super.init(map)
    }
    
    override open func mapping(_ map: Map) {
        super.mapping(map)
        myJokes         <- map["myJokes"]
        favoriteJokes   <- map["favoriteJokes"]
        blockedUsers    <- map["blockedUsers"]
        followingUsers  <- map["followingUsers"]
    }
}

// MARK: NSCopying

extension Login {
    
    public override func copyWithZone(_ zone: NSZone?) -> AnyObject { // <== NSCopying
        // *** Construct "one of my current class". This is why init() is a required initializer
        let theCopy = super.copyWithZone(zone) as! Login
        theCopy.recommendedJokes = self.recommendedJokes.map { $0.copy() as! Joke }
        theCopy.favoriteJokes = self.favoriteJokes.map { $0.copy() as! Joke }
        theCopy.myJokes = self.myJokes.map { $0.copy() as! Joke }
        theCopy.followingUsers = self.followingUsers.map { $0.copy() as! User }
        theCopy.blockedUsers = self.blockedUsers.map { $0.copy() as! User }
        return theCopy
    }
}
