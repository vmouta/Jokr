/**
 * @name             UsersTableViewController.swift
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

public enum UsersTypeView {
    case blocked
    case following
}

open class UsersTableViewController: TrackUITableViewController {
    
    open var users: [User]?
    open var logo: UIImage?
    
    open var type: UsersTypeView = .following
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        let imageView = UIImageView(image:logo)
        self.navigationItem.titleView = imageView
        self.navigationController?.navigationBar.tintColor = UIColor.white
    }
    
    override open func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let ident = segue.identifier, let jokesViewController = segue.destination as? JokesTableViewController, let selectedUserIndex = tableView.indexPathForSelectedRow?.row, ident == "JOKES" {
            jokesViewController.user = users?[selectedUserIndex]
            jokesViewController.type = .userJokes
            jokesViewController.logo = UIImage(named: "IconMyJokes")
        }
    }
    
    @IBAction func editUsers(_ sender: AnyObject) {
        self.tableView.setEditing(!self.tableView.isEditing, animated: true)
    }

}

// MARK: Table View Data Source

extension UsersTableViewController {
    
    override open func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return users?.count ?? 0
    }
    
    override open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) as! UserCell
        if let user = users?[indexPath.row] {
            cell.userName?.text = user.userName
            cell.followers?.text = String(user.numberOfFollowers)
            cell.jokesCount?.text = String(user.jokesCnt)
            cell.countryImage?.image = nil
            if let userID = user.serverID {
                JokrConnection.sharedInstance.downloadProfilePicure(userID){result in
                    switch(result) {
                    case .Success(let picture):
                        cell.userImage.image = picture
                        break
                    case .Failure(_):
                        break
                    }
                }
            }
        }
        return cell
    }
}

// Mark: Table View Delegate

extension UsersTableViewController {
    
    override public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.performSegue(withIdentifier: "JOKES", sender: self)
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    override public func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool
    {
        return true
    }
    
    override public func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == UITableViewCellEditingStyle.delete {
            switch(type) {
            case .blocked:
                let userToRemove = users?.remove(at: indexPath.row)
                if let loginID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID, let  userID = userToRemove?.serverID {
                    JokrConnection.sharedInstance.unblockUser(loginID, userToUnblockId: userID) {_ in
                        JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Blocked)
                    }
                }
            case .following:
                let userToRemove = users?.remove(at: indexPath.row)
                if let loginID = JokrAppDelegate.sharedInstance.login?.getUser()?.serverID, let  userID = userToRemove?.serverID {
                    JokrConnection.sharedInstance.unfollowUser(loginID, unfollowUserID: userID) {_ in
                        JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Following)
                    }
                }
            }
            tableView.deleteRows(at: [indexPath], with: UITableViewRowAnimation.automatic)
        }
    }
}

// Mark: Handling refreshing request

extension UsersTableViewController {
    
    @IBAction func refreshData(_ sender: AnyObject) {
        switch(type) {
        case .following:
            JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Following){
                self.refreshControl?.endRefreshing()
                self.users = JokrAppDelegate.sharedInstance.login?.followingUsers
                self.tableView.reloadData()
            }
        case .blocked:
            JokrAppDelegate.sharedInstance.updateApp(UpdateOptions.Blocked){
                self.refreshControl?.endRefreshing()
                self.users = JokrAppDelegate.sharedInstance.login?.blockedUsers
                self.tableView.reloadData()
            }
        }
    }
}




class UserCell: UITableViewCell {
    
    @IBOutlet weak var countryImage: UIImageView!
    @IBOutlet weak var userImage: UIImageView!
    
    @IBOutlet weak var userName: UILabel!
    @IBOutlet weak var jokesCount: UILabel!
    @IBOutlet weak var following: UILabel!
    @IBOutlet weak var followers: UILabel!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        userImage.layer.borderWidth = 1
        userImage.layer.masksToBounds = false
        userImage.layer.borderColor = UIColor.white.cgColor
        userImage.layer.cornerRadius = userImage.frame.height/4
        userImage.clipsToBounds = true
    }
    
    func setCountry(_ language: String) {
        switch(language) {
        case "en":
            countryImage.image = UIImage(named:"country_en")
        case "ch":
            countryImage.image = UIImage(named:"country_ch")
        case "de":
            countryImage.image = UIImage(named:"country_de")
        case "fr":
            countryImage.image = UIImage(named:"country_fr")
        case "it":
            countryImage.image = UIImage(named:"country_it")
        case "es":
            countryImage.image = UIImage(named:"country_es")
        case "pt":
            countryImage.image = UIImage(named:"country_pt")
        default:
            //TODO: Set a default image
            countryImage.image = nil
            break
        }
    }
}
