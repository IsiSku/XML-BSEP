USE [XML-BSEP]
GO
SET IDENTITY_INSERT [dbo].[Permission] ON 

INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(1 AS Numeric(19, 0)), N'Acts.cancelProcedure')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(2 AS Numeric(19, 0)), N'Acts.submitProposition')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(3 AS Numeric(19, 0)), N'Acts.submitAmendment')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(4 AS Numeric(19, 0)), N'Acts.submitFinal')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(5 AS Numeric(19, 0)), N'Acts.refuseDoc')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(6 AS Numeric(19, 0)), N'Acts.submitArchive')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(7 AS Numeric(19, 0)), N'Acts.submitXML')
INSERT [dbo].[Permission] ([id], [name]) VALUES (CAST(8 AS Numeric(19, 0)), N'AddUser.addUser')
SET IDENTITY_INSERT [dbo].[Permission] OFF
SET IDENTITY_INSERT [dbo].[Role] ON 

INSERT [dbo].[Role] ([id], [name]) VALUES (CAST(1 AS Numeric(19, 0)), N'Predsednik')
INSERT [dbo].[Role] ([id], [name]) VALUES (CAST(2 AS Numeric(19, 0)), N'Odbornik')
INSERT [dbo].[Role] ([id], [name]) VALUES (CAST(3 AS Numeric(19, 0)), N'Web Admin')
SET IDENTITY_INSERT [dbo].[Role] OFF
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(1 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(2 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(3 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(4 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(5 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(6 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(1 AS Numeric(19, 0)), CAST(7 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(2 AS Numeric(19, 0)), CAST(1 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(2 AS Numeric(19, 0)), CAST(2 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(2 AS Numeric(19, 0)), CAST(3 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(2 AS Numeric(19, 0)), CAST(6 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(2 AS Numeric(19, 0)), CAST(7 AS Numeric(19, 0)))
INSERT [dbo].[ROLE_PERMISSIONS] ([roles_id], [permissions_id]) VALUES (CAST(3 AS Numeric(19, 0)), CAST(8 AS Numeric(19, 0)))
SET IDENTITY_INSERT [dbo].[Users] ON 

INSERT [dbo].[Users] ([id], [LAST_NAME], [name], [password], [PASSWORD_SALT], [username], [role_id], [msgNum]) VALUES (CAST(13 AS Numeric(19, 0)), N'Adminjevic', N'Admin', N'PCrrm5+EwCrmxRLpa2T5C84aHUU=', N'6Bc/poxhfgg=', N'a@a.com', CAST(3 AS Numeric(19, 0)), CAST(0 AS Numeric(19, 0)))
INSERT [dbo].[Users] ([id], [LAST_NAME], [name], [password], [PASSWORD_SALT], [username], [role_id], [msgNum]) VALUES (CAST(14 AS Numeric(19, 0)), N'Odbornicic', N'Odbornik', N'50o+ATaSMRuRzHH2AkB0ghvnFvM=', N'qUsA28aup/Y=', N'o@a.com', CAST(2 AS Numeric(19, 0)), CAST(0 AS Numeric(19, 0)))
INSERT [dbo].[Users] ([id], [LAST_NAME], [name], [password], [PASSWORD_SALT], [username], [role_id], [msgNum]) VALUES (CAST(15 AS Numeric(19, 0)), N'Predsednicic', N'Predsednik', N'Fzk57BbfXZVAy5sDpeVaZuZTC0s=', N'E/5KwmNiAcU=', N'p@a.com', CAST(1 AS Numeric(19, 0)), CAST(0 AS Numeric(19, 0)))
SET IDENTITY_INSERT [dbo].[Users] OFF
