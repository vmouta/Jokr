/**
 * @name             JokeTableViewController.swift
 * @partof           zucred AG
 * @description
 * @author	 		Vasco Mouta
 * @created			03/01/16
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

import UIKit
import zucredApple

open class JokeViewController: TrackUIViewController {
    
    open var joke:Joke? = nil
    
    @IBOutlet weak var favoriteButton: UIButton!
    @IBOutlet weak var blockButton: UIButton!
    @IBOutlet weak var followButton: UIButton!
    @IBOutlet weak var shareButton: UIButton!
    
    @IBOutlet weak var shareLabel: UILabel!
    
    @IBOutlet weak var likeButton: UIButton!
    @IBOutlet weak var crapButton: UIButton!
    
    @IBOutlet weak var userName: UILabel!
    @IBOutlet weak var likes: UILabel!
    @IBOutlet weak var userImage: UIImageView!
    
    @IBOutlet weak var titleImageView: UIImageView!
    
    override open func viewDidLoad() {
        super.viewDidLoad()
     
        userImage.layer.borderWidth = 2
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.white.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/2
        userImage.clipsToBounds = true
        
        let titleButton: UIButton = UIButton(frame: CGRect(x: 0, y: 0, width: 100, height: 32))
        titleButton.setTitle(joke?.title, for: UIControlState())
        titleButton.titleLabel?.font = UIFont(name: "HelveticaNeue-UltraLight", size: 25.0)
        titleButton.setTitleColor(UIColor.white, for: UIControlState())
        //titleButton.addTarget(self, action: "titlePressed:", forControlEvents: UIControlEvents.TouchUpInside)
        self.navigationItem.titleView = titleButton
        
        if joke != nil {
            userName.text = joke!.userName
            likes.text = String(joke!.likes)
            if let currentUser = JokrAppDelegate.sharedInstance.login {
                favoriteButton.selected = currentUser.hasFavorite(joke!.modelID)
                blockButton.isSelected = currentUser.hasBlockedUser(joke!.userId)
                followButton.isSelected = currentUser.isFollowingUser(joke!.userId)
            }
            SwiftSpinner.show(joke!.title).addTapHandler({
                AppAudio.sharedInstance.pause()
                SwiftSpinner.hide()
            }, subtitle: "Tap to stop joke.").fillOnTouch = true
            if joke!.status == SyncStatus.NotSync.rawValue {
                AppAudio.sharedInstance.playFileAtPath(joke!.localUrl!, name:joke!.title , author:joke!.userName) { _ in
                    SwiftSpinner.hide()
                }
            } else {
                AppAudio.sharedInstance.streamFile(JokrConnection.Router.FetchJoke(jokeID: joke!.modelID).URL, name:joke!.title , author:joke!.userName) { _ in
                    SwiftSpinner.hide()
                }
            }

            JokrConnection.sharedInstance.downloadProfilePicure(joke!.userId) { response in
                switch(response) {
                case .Success(let image):
                    self.userImage.image = image
                default:
                    break
                }
                
            }
        } else {
            AppLogger.warning("No joke set")
        }
    }
    
    override open func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setLike(_ like:Bool) {
        likeButton.isSelected = like
        crapButton.isSelected = !like
    }

    @IBAction func likeJoke(_ sender: AnyObject) {
        if let jokeID = joke?.modelID, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID, likeButton.isSelected == false {
            likes.text = String(++joke!.likes)
            setLike(true)
            JokrConnection.sharedInstance.likeJoke(userdID, jokeId:jokeID) {_ in
                
            }
        } else {
            
        }
    }
    
    @IBAction func dislikeJoke(_ sender: AnyObject) {
        if let jokeID = joke?.modelID, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID, crapButton.isSelected == false {
            setLike(false)
            likes.text = String(--joke!.likes)
            JokrConnection.sharedInstance.dislikeJoke(userdID, jokeId:jokeID) {_ in 
                
            }
        } else {
            
        }
    }
    
    @IBAction func addFavorite(_ sender: AnyObject) {
        if let jokeID = joke?.modelID, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID {
            favoriteButton.isSelected = !favoriteButton.isSelected
            if(favoriteButton.isSelected) {
                JokrConnection.sharedInstance.addFavorite(userdID, jokeId:jokeID) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites)
                }
            } else {
                JokrConnection.sharedInstance.removeFavorite(userdID, jokeId:jokeID) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Favorites)
                }
            }
        } else {
            AppLogger.error("Not possible to change favorite due to nil jokeid or login serverID")
        }
    }
    
    @IBAction func blockUser(_ sender: AnyObject) {
        if let userToBlock = joke?.userId, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID {
            blockButton.isSelected = !blockButton.isSelected
            if(blockButton.isSelected) {
                JokrConnection.sharedInstance.blockUser(userdID, userToBlockId:userToBlock) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Blocked)
                }
            } else {
                JokrConnection.sharedInstance.unblockUser(userdID, userToUnblockId:userToBlock) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Blocked)
                }
            }
        } else {
            AppLogger.error("Not possible to block user due to nil joke userId or login serverID")
        }
    }
    
    @IBAction func followUser(_ sender: AnyObject) {
        if let userToFollow = joke?.userId, let userdID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID {
            followButton.isSelected = !followButton.isSelected
            if(followButton.isSelected) {
                JokrConnection.sharedInstance.followUser(userdID, followUserID: userToFollow) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Following)
                }
            } else {
                JokrConnection.sharedInstance.unfollowUser(userdID, unfollowUserID: userToFollow) {_ in
                    JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Following)
                }
            }
            
        } else {
            AppLogger.error("Not possible to share due to nil joke or share url")
        }
    }
    
    @IBAction func shareJoke(_ sender: AnyObject) {
        if let jokeID = joke?.shareID {
            if(joke!.isLocal) {
                showMessage("Upload joke first!", type: .Error, options: [])
            } else {
                let vc = UIActivityViewController(activityItems: [JokrConnection.Router.fetchJoke(jokeID: jokeID).URL], applicationActivities: [])
                present(vc, animated: true, completion: nil)
            }
        } else {
            AppLogger.error("Not possible to share due to nil joke or share url")
        }
    }
}
